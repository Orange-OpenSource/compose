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

package users.controllers

import applications.model.dao.Application
import common.controllers.{AbstractIdentityController, ErrorHandler, JsonSanitizer}
import login.dao.TokensDao
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.dao.UserDao
import users.model.body.{UserInput, UserInputUpdate}
import users.model.dao.User

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class UserController @Inject()(
                                userDao: UserDao,
                                tokensDao: TokensDao,
                                userManager: UsersManager,
                                implicit val ec: ExecutionContext,
                                cc: ControllerComponents
                              ) extends AbstractIdentityController[User](cc, userDao, userDao, tokensDao, ec, User.format) with ErrorHandler {

  override val sanitize: User => JsValue = JsonSanitizer()("userId")(User.format)
  val sanitizeApp: Application => JsValue = JsonSanitizer(__ \ "hashedApiKey", __ \ "salt")("applicationId")(Application.format)

  def getMachines(userId: String): Action[AnyContent] = auth.authorized(List("ADMIN"))(parse.json).async {
    entering("getMachines")
    userManager.getMachines(userId).map(
      machines => {
        Ok(Json.toJson(machines.map(sanitizeApp)))
      }
    ).recover {
      case exception: Exception =>
        handleError("getMachines", exception)
    }
  }


  override def create(): Action[JsValue] = auth.authorized(List("ADMIN"))(parse.json).async { request =>
    entering("create")
    request.body.validate[UserInput].fold(
      errors => handleValidationErrors(errors),
      user => {
        userManager.createUser(user).map(
          newUser => {
            Created(sanitize(newUser))
          }
        )
      }
    ).recover {
      case exception: Exception =>
        handleError("create", exception)
    }

  }

  override def update(userId: String): Action[JsValue] = auth.authorized(List("ADMIN"))(parse.json).async { request =>
    entering("update")
    request.body.validateOpt[UserInputUpdate].fold(
      errors => handleValidationErrors(errors),
      user => {
        userManager.updateUser(user.get, userId).map({
          updatedUser => {
            Ok(sanitize(updatedUser))
          }
        })
      }

    ).recover {
      case exception: Exception =>
        handleError("update", exception)
    }
  }
}
