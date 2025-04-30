/*
 *
 *  * Software Name : Compose
 *  * SPDX-FileCopyrightText: Copyright (c) Orange SA
 *  * SPDX-License-Identifier:  MIT
 *  *
 *  * This software is distributed under the MIT License,
 *  * see the "LICENSE.txt" file for more details or https://spdx.org/licenses/MIT.html
 *  *
 *  * <Authors: optional: authors list / see CONTRIBUTORS>
 *
 */

package smart_contracts.controllers

import applications.dao.ApplicationDao
import common.controllers.{AuthorizedWithApiKey, ErrorHandler}
import common.exceptions.BadRequestException
import common.utils.{CryptoUtils, TraceUtils}
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc._
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.controllers.blockchain.ethereum.EthereumManager
import smart_contracts.controllers.blockchain.factory.BlockchainFactory
import smart_contracts.controllers.vm.evm.EvmSmartContract
import smart_contracts.dao.AbiDao
import smart_contracts.model.body.{FunctionParameters, ReadFunctionParameters}
import wallets.controllers.ethereum.EthereumWalletManager
import wallets.dao.WalletDao
import wallets.utils.EthereumWalletUtils

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SmartContractController @Inject()(applicationDao: ApplicationDao,
                                        abiDao: AbiDao,
                                        walletDao: WalletDao,
                                        blockchainFactory: BlockchainFactory,
                                        configuration: Configuration,
                                        implicit val ec: ExecutionContext,
                                        cc: ControllerComponents
                                       ) extends AbstractController(cc) with TraceUtils with ErrorHandler {

  private val auth = AuthorizedWithApiKey(applicationDao, cc, ec)


  def pureOrViewFunction(blockchain: String, contractAddress: String, functionName: String): Action[ReadFunctionParameters] = auth.authorized(parse.json[ReadFunctionParameters]).async { request =>
    entering("pureOrViewFunction")
    val params = request.body
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    walletDao
      .findById(request.user.walletId)
      .flatMap(wallet => {
        val senderAddress: String = if (params.wallet.isDefined) {
          params.wallet.get.address
        } else {
          wallet.address
        }

        abiDao
          .findBy("address")(contractAddress)
          .map(contracts => contracts.find(elt => elt.blockchain == blockchain).get)
          .map(abi => EvmSmartContract(abi))
          .map(sc => {

            val function = sc.findFunction(functionName, params.parameters).get
            Ok(Json.toJson(sc.prepareAndInvokeView(function, params.parameters, senderAddress)(bcM).get.map(_.toJson.get)))
          })
      })
  }



  def asyncFunction(blockchain: String, contractAddress: String, functionName: String): Action[FunctionParameters] = auth.authorized.async(parse.json[FunctionParameters]) { request =>
    entering("asyncFunction")
    val params = request.body
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)

    val walletManager = EthereumWalletManager()

    walletDao.findById(request.user.walletId).flatMap(wallet => {
      val password = CryptoUtils(configuration).retrieveWalletKey(wallet.salt, params.walletPassword)
      val walletFile = walletManager.loadWallet(wallet.file)
      val credentials = EthereumWalletUtils().loadCredentials(password, walletFile)

      abiDao
        .findBy("address")(contractAddress)
        .map(contracts => contracts.find(elt => elt.blockchain == blockchain).get)
        .map(abi => EvmSmartContract(abi))
        .map(sc => {
          val function = sc.findFunction(functionName, params.parameters).get
          Ok(
            s"""
               |{
               |   "hash": "${sc.prepareAndInvoke(function, params.tokenValue.getOrElse(0) ,params.parameters)(bcM, credentials).get}"
               |}
               |""".stripMargin)
        })
    })

  }

  def transactionReceipt(blockchain: String, receiptId: String): Action[Unit] = auth.authorized(parse.empty) { _ =>
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType,configuration,blockchain)
    bcM
      .transactionReceipt(receiptId)
      .map(receipt => Ok(Json.toJson(receipt)))
      .recover {
        case exception: Exception => handleError("transactionReceipt", exception)
      }.get

  }

  def getBlockNumber(blockchain: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(
      s"""
        {
          "block":"${bcM.getBlockNumber}"
        }
    """)
  }

  def getBlockByNumber(blockchain: String, number: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(Json.toJson(bcM.getBlockByNumber(number)))
  }

  def getBlockByHash(blockchain: String, hash: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(Json.toJson(bcM.getBlockByHash(hash)))
  }

  def getCodeAt(blockchain: String, contractAddress: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(bcM.getCodeAt(contractAddress))
  }

  def getPeersNumber(blockchain: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(bcM.getPeersNumber.toString)
  }

  def getProtocolVersion(blockchain: String): Action[AnyContent] = auth.authorized(parse.empty) {
    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    Ok(bcM.getProtocolVersion)
  }

}
