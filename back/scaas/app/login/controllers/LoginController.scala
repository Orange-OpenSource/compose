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

package login.controllers

import common.controllers.ErrorHandler
import common.exceptions.{AuthException, LoginException}
import login.model.LoginInput
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{AbstractController, Action, ControllerComponents}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LoginController @Inject()(
                                 loginManager: LoginManager,
                                 implicit val ec: ExecutionContext,
                                 cc: ControllerComponents
                               ) extends AbstractController(cc) with ErrorHandler {


  def login(): Action[JsValue] = Action.async(parse.json) { req =>
    entering("login")
    req.body.validate[LoginInput].fold(
      errors => handleValidationErrors(errors),
      u => {
        loginManager.isLoginOk(u).map(token => {
          Ok(Json.toJson(token))
        })
      }
    ) recover {
      case _: Exception =>
        handleError("login", LoginException("Wrong login/password"))
    }
  }
}
