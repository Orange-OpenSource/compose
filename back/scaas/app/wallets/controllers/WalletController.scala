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

package wallets.controllers

import better.files.File
import com.fasterxml.jackson.databind.ObjectMapper
import common.controllers.{AuthorizedWithSIU, ErrorHandler, JsonSanitizer}
import common.exceptions.ParseException
import login.dao.TokensDao
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsValue, Json, __}
import play.api.mvc._
import smart_contracts.controllers.blockchain.ethereum.EthereumManager
import smart_contracts.model.body.ImportWallet
import users.dao.UserDao
import wallets.controllers.factory.WalletFactory
import wallets.dao.WalletDao
import wallets.model.body.{Balance, ConfigWallet}
import wallets.model.dao.Wallet

import java.nio.file.Paths
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class WalletController @Inject()(
                                  walletDBDAO: WalletDao,
                                  userDao: UserDao,
                                  tokensDao: TokensDao,
                                  walletFactory: WalletFactory,
                                  implicit val ec: ExecutionContext,
                                  configuration: Configuration,
                                  cc: ControllerComponents
                                )
  extends AbstractController(cc) with ErrorHandler {

  val sanitize: Wallet => JsValue = JsonSanitizer(__ \ "file", __ \ "salt")("walletId")(Wallet.format)
  private val auth = AuthorizedWithSIU(userDao, tokensDao, cc, ec)
  private val objectMapper = new ObjectMapper()
  private val projectDirectory: better.files.File = better.files.File(s"${configuration.get[String]("project.repository")}")
  def initProjectsDirectories(): Any = {
    if (projectDirectory.notExists) {
      projectDirectory.createDirectories()
    }
  }
  def createWallet(userId: String): Action[JsValue] = auth.authorized(List("USER"))(parse.json).async { request =>
    entering("createWallet")

    request
      .body
      .validate[ConfigWallet]
      .asOpt
      .map(configWallet => {

        val WM = walletFactory.build(configuration, configWallet.blockchain)

        WM.generateWallet(configWallet.name, configWallet.password, configWallet.blockchain, userId)(configuration)
      })
      .map(wallet => {
        walletDBDAO.create(wallet)
      })
      .getOrElse(Future.failed[Wallet](ParseException("Impossible to parse body")))
      .map(w => Created(sanitize(w)))
      .recover {
        case exception: Exception =>
          handleError("createWallet", exception)
      }


  }

  def importWallet(userId: String): Action[MultipartFormData[TemporaryFile]] = auth.authorized(List("USER"))(parse.multipartFormData).async { request =>
    entering("importWallet")
    initProjectsDirectories()
    val file = request.body.file("walletFile").head.ref
    val body = Json.parse(request.body.dataParts("myBody").head)

    body.validate[ImportWallet].fold(
      errors => handleValidationErrors(errors),
      importWallet => {
        val pwWallet = importWallet.walletPassword
        val configWallet: ConfigWallet = importWallet.configWallet
        val filename = file.getFileName.toString
        file.copyTo(Paths.get(s"$projectDirectory/$filename"), replace = true)
        val walletJson = File(s"$projectDirectory/$filename")
        val walletContent = walletJson.contentAsString
        walletJson.delete()
        val WM = walletFactory.build(configuration, configWallet.blockchain)

        val keyPair = WM.getKeyPair(walletContent, pwWallet)

        val wallet = WM.importWallet(configWallet.name, configWallet.password, keyPair, configWallet.blockchain, userId)(configuration)

        walletDBDAO.create(wallet).map(w => {
          Created(sanitize(w))
        })
      }).recover {
      case exception: Exception =>
        handleError("importWallet", exception)
    }

  }

  def getWallet(userId: String, blockchain: String, walletAddress: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("getWallet")

    walletDBDAO
      .findOneByAddress(userId, blockchain, walletAddress)
      .map(wallet => Ok(sanitize(wallet)))
      .recover {
        case exception: Exception =>
          handleError("getWallet", exception)
      }

  }

  def exportWallet(userId: String, blockchain: String, walletAddress: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("exportWallet")

    walletDBDAO
      .findOneByAddress(userId, blockchain, walletAddress)
      .map(wallet => Ok(wallet.file))
      .recover {
        case exception: Exception =>
          handleError("getWallet", exception)
      }

  }

  def getWalletsByUserId(userId: String): Action[AnyContent] = auth.authorized(List("USER"))(parse.anyContent).async { req =>
    entering("getWalletsByUserId")

    walletDBDAO.findBy("userId")(userId).map(wallets => {
      Ok(Json.toJson(wallets.map(sanitize)))
    }).recover {
      case exception: Exception =>
        handleError("getWalletsByUserId", exception)
    }
  }

  def deleteWallet(userId: String, blockchain: String, walletAddress: String): Action[Unit] = auth.authorized(List("USER"))(parse.empty).async { request =>
    entering("deleteWallet")

    walletDBDAO.findOneByAddress(userId, blockchain, walletAddress)
      .flatMap(wallet => {
        if (wallet.userId == userId) {
          walletDBDAO.deleteById(wallet._id.get).map(_ => {
            NoContent
          })
        } else {
          Future.successful(Unauthorized)
        }
      }).recover {
      case exception: Exception =>
        handleError("deleteWallet", exception)
    }

  }

  def getBalance(userId: String, blockchain: String, walletAddress: String): Action[Unit] = auth.authorized(List("USER"))(parse.empty) { request =>
    entering("getBalance")
    val ethManager = EthereumManager(blockchain, configuration)
    Ok(Json.toJson(Balance(ethManager.getBalance(walletAddress))))
  }


}
