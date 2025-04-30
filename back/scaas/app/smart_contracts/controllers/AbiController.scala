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

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.utils.{CryptoUtils, TraceUtils}
import login.dao.TokensDao
import org.web3j.crypto.WalletUtils
import play.api.ConfigLoader.seqStringLoader
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import smart_contracts.controllers.blockchain.ethereum.EthereumManager
import smart_contracts.controllers.blockchain.factory.BlockchainFactory
import smart_contracts.dao.AbiDao
import smart_contracts.model.body.AbiInput
import smart_contracts.model.dao.Abi
import users.dao.UserDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext

class AbiController @Inject()(abiDao: AbiDao,
                              userDao: UserDao,
                              tokensDao: TokensDao,
                              configuration: Configuration,
                              blockchainFactory:BlockchainFactory,
                              implicit val ec: ExecutionContext,
                              cc: ControllerComponents
                             ) extends AbstractIdentityController[Abi](cc, abiDao, userDao, tokensDao, ec, Abi.format) with TraceUtils {

  val sanitize: Abi => JsValue = JsonSanitizer()("contractId")(Abi.format)


  def retrieveAbis(userId: String): Action[AnyContent] = auth.authorized(List("USER")).async { request =>
    entering("retrieveAll")

    abiDao.findBy("userId")(userId).map(contracts => Ok(Json.toJson(contracts.filter(contract => contract.userId == userId).map(sanitize))))
      .recover {
        case e: Exception => handleError("retrieveAll", e)
      }
  }

  def retrieveAbisForBlockchain(blockchain: String): Action[AnyContent] = auth.authorized(List("USER")).async { request =>
    entering("retrieveAll")
    val userId = request.user._id.get
    abiDao.findBy("userId")(userId)
      .map(contracts => Ok(Json.toJson(contracts.filter(contract => contract.blockchain == blockchain).filter(contract => contract.userId == userId).map(sanitize))))
      .recover {
        case e: Exception => handleError("retrieveAll", e)
      }
  }

  def retrieve(userId: String, address: String): Action[AnyContent] = auth.authorized(List("USER")).async { request =>
    entering("retrieve")

    abiDao.findBy("userId")(userId)
      .map(contracts => contracts
        .filter(contract => contract.userId == userId).find(abi => abi.address == address).get
      ).map(abi => Ok(sanitize(abi)))
      .recover {
        case e: Exception => handleError("retrieve", e)
      }

  }

  def createAbi(userId: String, contractAddress: String): Action[JsValue] = auth.authorized(List("USER"))(parse.json).async { request =>
    entering("createAbi")

    if(!WalletUtils.isValidAddress(contractAddress)){
      BadRequest("address not well formatted")
    }
    request.body.validate[AbiInput].fold(
      errors => handleValidationErrors(errors),
      abiInput => {

        val newAbi: Abi = Abi(
          _id = Some(randomUUID.toString),
          name = abiInput.name,
          blockchain = abiInput.blockchain,
          abi = abiInput.abi,
          address = contractAddress,
          userId = userId
        )
        abiDao.create(newAbi).map(createdAbi => {
          Created(sanitize(createdAbi))
        })

      }).recover {
      case exception: Exception =>
        handleError("create", exception)
    }

  }

  def deleteAbi(userId: String, blockchain: String, address: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("deleteAbi")

    abiDao.findBy("address")(address)
      .flatMap(abis => {
        val abi = abis.find(abi => (abi.blockchain == blockchain) && abi.userId == userId).get
        abiDao.deleteById(abi._id.get).map(_ => NoContent)
      }).recover {
      case exception: Exception =>
        handleError("deleteAbi", exception)
    }

  }

  def deleteById(userId: String, id: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>

    abiDao.deleteById(id).map(_ => NoContent).recover {
      case exception: Exception =>
        handleError("deleteById", exception)
    }

  }

  def getActiveBlockchains: Action[Unit] = auth.authorized(List("USER"))(parse.empty) { _ =>
    val blockchains = configuration.get("blockchain.active").toList
    Ok(Json.toJson(blockchains))
  }

  def getBlockchain(blockchain: String): Action[AnyContent] = auth.authorized(List("USER"))(parse.empty) {

    val bcType = configuration.get[String](s"blockchain.$blockchain.type")
    val strBlockchain = "blockchain."
    val bcM = blockchainFactory.build(bcType, configuration, blockchain)
    val infos =
      s"""
         |{
         | "name":"$blockchain",
         | "type":"${configuration.get[String](strBlockchain+ blockchain + ".type")}",
         | "web3Client": {
         |   "version":"${bcM.web3Version()}",
         |   "url":"${configuration.get[String](strBlockchain + blockchain + ".url")}"
         | },
         | "chainId":"${bcM.getChainId}",
         | "blockNumber":"${bcM.getBlockNumber}",
         | "gasPrice":"${bcM.getGasPrice}",
         | "protocol":"${bcM.getProtocolVersion}",
         | "explorer":"${configuration.get[String](strBlockchain + blockchain + ".explorer")}"
         |}
         |""".stripMargin


    Ok(infos)
  }

}
