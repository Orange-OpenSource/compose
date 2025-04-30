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

package companies.controllers

import common.controllers.{AbstractIdentityController, ErrorHandler, JsonSanitizer}
import companies.dao.CompanyDao
import companies.model.body.CompanyInput
import companies.model.dao.Company
import login.dao.TokensDao
import play.api.libs.json._
import play.api.mvc._
import users.dao.UserDao
import users.model.dao.User

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class CompanyController @Inject()(

                                   companyRepo: CompanyDao,
                                   userDao: UserDao,
                                   tokensDao: TokensDao,
                                   companyManager: CompaniesManager,
                                   implicit val ec: ExecutionContext,
                                   cc: ControllerComponents

                                 ) extends AbstractIdentityController[Company](cc, companyRepo, userDao, tokensDao, ec, Company.format) with ErrorHandler {
  val sanitize: Company => JsValue = JsonSanitizer()("companyId")(Company.format)
  private val sanitizeUser: User => JsValue = JsonSanitizer()("userId")(User.format)


  def getUsers(companyId: String): Action[AnyContent] = auth.authorized(List("ADMIN"))(parse.anyContent).async { req =>
    entering("getUsers")
    userDao.findBy("companyId")(companyId).map(seqUser => {
      Ok(Json.toJson(seqUser.filter(user => user.companyId == companyId).map(sanitizeUser)))
    })
      .recover {
        case exception: Exception =>
          handleError("getUsers", exception)
      }
  }

  override def create(): Action[JsValue] = auth.authorized(List("SUPER"))(parse.json).async { request =>
    entering("create")
    request.body.validate[CompanyInput].fold(
      errors => handleValidationErrors(errors),
      company => {
        companyManager.createCompany(company).map(
          result => {
            Created(sanitize(result))
          }
        )
      }.recover {
        case exception: Exception =>
          handleError("create", exception)
      }
    )
  }
}
