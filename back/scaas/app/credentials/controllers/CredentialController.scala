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

package credentials.controllers

import common.controllers.{AbstractIdentityController, ErrorHandler, JsonSanitizer}
import credentials.dao.CredentialDao
import credentials.model.body.CredentialInput
import credentials.model.dao.Credential
import login.dao.TokensDao
import play.api.libs.json._
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import users.dao.UserDao
import users.model.dao.User

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CredentialController @Inject()(
                                      credentialRepo: CredentialDao,
                                      credentialManager: CredentialsManager,
                                      userDao: UserDao,
                                      tokensDao: TokensDao,
                                      implicit val ec: ExecutionContext,
                                      cc: ControllerComponents
                                    ) extends AbstractIdentityController[Credential](cc, credentialRepo, userDao, tokensDao, ec, Credential.format) with ErrorHandler {

  override val sanitize: Credential => JsValue = JsonSanitizer(__ \ "otp", __ \ "salt", __ \ "hashedPassword")("credentialId")

  def create(userId: String): Action[JsValue] = auth.authorized(List("USER"))(parse.json).async { request =>
    entering("create")
    request.body.validate[CredentialInput].fold(
      errors => handleValidationErrors(errors),
      credential => {
        credentialManager.createFromPassword(credential.password).map(
          newCredential => {
            userDao.findById(userId).map(user => {
              userDao.updateById(userId, User(Some(userId), user.name, user.familyName, user.email, user.telephone, newCredential._id.get, user.machines, user.roles, user.companyId))
            })
            Created(sanitize(newCredential))
          }
        )
      }
    ).recover {
      case exception: Exception =>
        handleError("create", exception)
    }
  }

  override def update(userId: String): Action[JsValue] = auth.authorized(List("USER"))(parse.json).async { req =>
    entering("update")
    req.body.validate[CredentialInput].fold(
      errors => handleValidationErrors(errors),
      credential => {
        credentialManager.updateCredential(userId, credential).map(
          result => {
            Ok(sanitize(result))
          }
        )
      }
    ).recover {
      case exception: Exception =>
        handleError("update", exception)
    }
  }

  def deleteByUserId(userId: String): Action[AnyContent] = auth.authorized(List("USER")).async { req =>
    entering("delete")

    userDao.findById(userId).map(user => {
      val updatedUser: User = User(
        _id = user._id,
        name = user.name,
        familyName = user.familyName,
        email = user.email,
        telephone = user.telephone,
        credential = "",
        machines = user.machines,
        roles = user.roles,
        companyId = user.companyId
      )
      credentialRepo.deleteById(user.credential).map(_ => userDao.updateById(userId, updatedUser))
      NoContent
    }).recover {
      case exception: Exception =>
        handleError("delete", exception)
    }
  }
}
