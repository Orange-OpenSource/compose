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

package applications.controllers

import applications.dao.ApplicationDao
import applications.model.body.ApplicationInput
import applications.model.dao.Application
import common.controllers.{AbstractIdentityController, ErrorHandler, JsonSanitizer}
import login.dao.TokensDao
import play.api.libs.json.{JsValue, Json, __}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.dao.UserDao
import users.model.dao.User

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ApplicationsController @Inject()(
                                        machineRepo: ApplicationDao,
                                        userDao: UserDao,
                                        machineManager: ApplicationsManager,
                                        tokensDao: TokensDao,
                                        implicit val ec: ExecutionContext,
                                        cc: ControllerComponents
                                      ) extends AbstractIdentityController[Application](cc, machineRepo, userDao, tokensDao, ec, Application.format) with ErrorHandler {

  val sanitize: Application => JsValue = JsonSanitizer(__ \ "hashedApiKey", __ \ "salt")("applicationId")(Application.format)

  def create(userId: String): Action[JsValue] = auth.authorized(List("USER"))(parse.json).async { request =>
    entering("create")

      request.body.validate[ApplicationInput].fold(
        errors => handleValidationErrors(errors),
        machine => {

          machineManager.createMachine(userId, machine.blockchain, machine).map(
            result => {
              val apiKey = result._1
              val applicationId = result._2
              val jsonKey = s"""{"api_key":"$apiKey", "application_id":"$applicationId"}"""
              Created(jsonKey)
            }
          )
        }.recover {
          case exception: Exception =>
            handleError("create", exception)
        }
      )

  }

  def delete(userId: String, appId: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("delete")
      machineRepo.deleteById(appId).map(_ => {
        userDao.findById(userId).map(user => {

          val updatedUser: User = User(
            _id = user._id,
            name = user.name,
            familyName = user.familyName,
            email = user.email,
            telephone = user.telephone,
            credential = user.credential,
            machines = user.machines.filter(_ != appId),
            roles = user.roles,
            companyId = user.companyId
          )
          userDao.updateById(userId, updatedUser)
        })
        NoContent
      }).recover {
        case exception: Exception =>
          handleError("delete", exception)
      }

  }

  def getAllUserApplications(userId: String): Action[AnyContent] = auth.authorized(List("USER"))(parse.anyContent).async { req =>
    entering("getAllUserApplications")

      userDao.findById(userId)
        .flatMap(user => {
          Future.sequence(user
            .machines
            .map(machineId => {
              machineRepo.findOneBy("_id")(machineId)
            })).map(applications => applications.map(application => sanitize(application)))
            .map(jsApplications => Ok(Json.toJson(jsApplications)))
        }).recover {
        case exception: Exception =>
          handleError("getAllUserApplications", exception)
      }
  }

  def getApplicationById(userId: String, id: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("getApplicationById")
      userDao.findById(userId).flatMap(
        user => {
          if (user.machines.contains(id)) {
            machineRepo.findById(id).map(app => {
              Ok(sanitize(app))
            })
          } else {
            Future.successful(Unauthorized)
          }
        }

      ).recover {
        case exception: Exception =>
          handleError("getApplicationById", exception)
      }

  }
}
