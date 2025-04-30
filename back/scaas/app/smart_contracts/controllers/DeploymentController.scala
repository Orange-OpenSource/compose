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
import org.joda.time.DateTime
import play.api.Configuration
import play.api.libs.Files.TemporaryFile
import play.api.libs.json.{JsObject, JsString, JsValue, Json}
import play.api.mvc._
import smart_contracts.dao.{ContractDBDAO, OngoingDeploymentDAO}
import smart_contracts.model.ContractBody
import smart_contracts.model.dao.{Contract, OngoingDeployment}
import users.dao.UserDao
import wallets.controllers.ethereum.EthereumWalletManager
import wallets.dao.WalletDao
import wallets.utils.EthereumWalletUtils

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext


class DeploymentController @Inject()(
                                      contractRepo: ContractDBDAO,
                                      userDao: UserDao,
                                      tokensDao: TokensDao,
                                      ongoingDeploymentDAO: OngoingDeploymentDAO,
                                      contractManager: ContractsManager,
                                      configuration: Configuration,
                                      walletDao: WalletDao,
                                      implicit val ec: ExecutionContext,
                                      cc: ControllerComponents
                                    ) extends AbstractIdentityController[Contract](cc, contractRepo, userDao, tokensDao, ec, Contract.format) with TraceUtils {

  override val sanitize: Contract => JsValue = JsonSanitizer()("deploymentId")(Contract.format)
  val sanitizeOngoing: OngoingDeployment => JsValue = JsonSanitizer()("deploymentId")(OngoingDeployment.format)

  def createContracts(userId: String): Action[MultipartFormData[TemporaryFile]] = auth.authorized(List("USER")).async(parse.multipartFormData) { request =>
    entering("createContracts")

    val file = request.body.file("projectFile").head.ref
    val body = Json.parse(request.body.dataParts("myBody").head)
    val projectID = randomUUID.toString

    body.validate[ContractBody].fold(
      errors => handleValidationErrors(errors),
      contractBody => {
        val walletInfos = contractBody.wallet
        val walletManager = EthereumWalletManager()
        walletDao
          .findOneByAddress(userId, contractBody.options.network, walletInfos.address)
          .flatMap(wallet => {
            val password = CryptoUtils(configuration).retrieveWalletKey(wallet.salt, walletInfos.password)
            debug("createContracts","walletKey retrieved (password)")
            val walletFile = walletManager.loadWallet(wallet.file)
            debug("createContracts","wallet Loaded")
            val credentials = EthereumWalletUtils().loadCredentials(password, walletFile)
            debug("createContracts","credentials loaded")
            contractManager.uploadFile(file, body, projectID, userId, contractBody.options.network)(credentials)
            debug("createContracts","deleting ongoingDeployment with project id :"+projectID)
            ongoingDeploymentDAO.deleteById(projectID)


          })

        ongoingDeploymentDAO.create(
          OngoingDeployment(
            projectID,
            contractBody.options.network,
            request.user._id.get,
            DateTime.now().getMillis.toString
          )

        ).map(og => {
          Ok(
            s"""
               |{
               | "deploymentId":"$projectID"
               |}
               |""".stripMargin)
        }).recover {
          case ex: Exception => handleError("ongoing create", ex)
        }


      }
    ).recover {
      case ex: Exception => handleError("createContracts", ex)
    }

  }

  def getOngoingDeploymentsForUser(userId: String): Action[AnyContent] = auth.authorized(List("USER")).async(parse.anyContent) { request =>
    entering("getForUser")

    ongoingDeploymentDAO
      .findBy("userId")(userId)
      .map(ongoings => Ok(Json.toJson(ongoings.map(sanitizeOngoing))))
      .recover {
        case ex: Exception => handleError("getForUser", ex)
      }

  }

  private def caviard(contracts: Seq[Contract], userId: String) = {
    entering("caviard")

    Ok(Json.toJson(contracts
      .filter(contract => contract.owner == userId)
      .map(contract => {
        val json = Json.parse(contract.file)
        val abi = json \ "abi"
        val networks = json \ "networks"
        val updatedAt = json \ "updatedAt"
        val bytecode = json \ "bytecode"
        val deployedBytecode = json \ "deployedBytecode"
        val compiler = json \ "compiler"

        JsObject(
          Seq(
            "deploymentId" -> JsString(contract.project),
            "name" -> JsString(contract.name),
            "userId" -> JsString(contract.owner),
            "blockchain" -> JsString(contract.blockchain),
            "abi" -> abi.getOrElse(JsString("")),
            "compiler" -> compiler.getOrElse(JsString("")),
            "bytecode" -> bytecode.getOrElse(JsString("")),
            "deployedBytecode" -> deployedBytecode.getOrElse(JsString("")),
            "networks" -> networks.getOrElse(JsString("")),
            "updatedAt" -> updatedAt.getOrElse(JsString(""))
          )
        )

      })))
  }

  def getForUser(userId: String): Action[AnyContent] = auth.authorized(List("USER")).async(parse.anyContent) { request =>
    entering("getForUser")

    contractRepo
      .findBy("owner")(userId)
      .map(contracts => caviard(contracts, userId))
      .recover {
        case ex: Exception => handleError("getForUser", ex)
      }


  }

  def getDeployment(userId: String, projectId: String): Action[AnyContent] = auth.authorized(List("USER")).async(parse.anyContent) { request =>
    entering("getDeployment")

    contractRepo
      .findBy("project")(projectId)
      .map(contracts => caviard(contracts, userId))
      .recover {
        case ex: Exception => handleError("getDeployment", ex)
      }


  }
}
