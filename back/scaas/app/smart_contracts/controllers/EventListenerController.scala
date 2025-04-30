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
import common.utils.TraceUtils
import login.dao.TokensDao
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, ControllerComponents}
import smart_contracts.dao.WebhookListenersDao
import smart_contracts.model.body.WebhookInput
import smart_contracts.model.dao.WebhookListener
import users.dao.UserDao

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventListenerController @Inject()(webhooksDao: WebhookListenersDao,
                                        userDao: UserDao,
                                        tokensDao: TokensDao,
                                        implicit val ec: ExecutionContext,
                                        cc: ControllerComponents
                                       ) extends AbstractIdentityController[WebhookListener](cc, webhooksDao, userDao, tokensDao, ec, WebhookListener.format) with TraceUtils {

  val sanitize: WebhookListener => JsValue = JsonSanitizer()("webhookId")(WebhookListener.format)


  def createWebhook(userId: String, contractAddress: String, eventName: String): Action[JsValue] = auth.authorized(List("USER")).async(parse.json) { request =>

    request.body.validate[WebhookInput].fold(
      errors => handleValidationErrors(errors),
      webhookInput => {
        val listener = WebhookListener(UUID.randomUUID().toString, userId, webhookInput.blockchain, contractAddress, eventName, webhookInput.url)
        webhooksDao
          .create(listener)
          .map(listener => Created(sanitize(listener)))
          .recover {
            case ex: Exception => handleError("createWebhook", ex)
          }
      }
    ).recover {
      case exception: Exception =>
        handleError("createWebhook", exception)
    }

  }

  def getWebhook(blockchain: String, contractAddress: String, eventName: String, id: String): Action[Unit] = auth.authorized(List("USER")).async(parse.empty) { request =>
    webhooksDao
      .findById(id)
      .filter(hook => hook.userId == request.user._id.get)
      .map(webHook => Ok(sanitize(webHook)))
      .recover {
        case ex: Exception => handleError("getWebhook", ex)
      }
  }

  def getAllWebhook(userId: String, contractAddress: String, eventName: String): Action[Unit] = auth.authorized(List("USER")).async(parse.empty) { request =>

    webhooksDao
      .findBy("userId")(userId)
      .map(webHook => Ok(Json.toJson(webHook.filter(webhooks => webhooks.contractAddress == contractAddress ).map(sanitize))))
      .recover {
        case ex: Exception => handleError("getWebhook", ex)
      }

  }

  def deleteWebHook(blockchain: String, contractAddress: String, eventName: String, id: String): Action[Unit] = auth.authorized(List("USER")).async(parse.empty) { request =>
    webhooksDao
      .findById(id)
      .flatMap(webHook => {
        if (webHook.userId == request.user._id.get) {
          webhooksDao
            .deleteById(id)
            .map(_ => NoContent)
            .recover {
              case ex: Exception => handleError("getWebhook", ex)
            }
        } else {
          Future.successful(handleError("getWebhook", new Exception("Not Found")))
        }

      })
  }

}
