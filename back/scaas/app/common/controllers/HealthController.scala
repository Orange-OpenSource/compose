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

package common.controllers

import common.utils.TraceUtils
import companies.controllers.CompaniesManager
import companies.model.body.CompanyInput
import play.api.Configuration
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import scaasInfos.BuildInfo
import smart_contracts.controllers.ContractsManager
import smart_contracts.dao.{ContractDBDAO, LogDao}
import users.controllers.UsersManager
import users.dao.UserDao
import users.model.body.UserInput

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global

class HealthController @Inject()(components: ControllerComponents,
                                 companiesManager: CompaniesManager,
                                 userDao: UserDao,
                                 userManager: UsersManager,
                                 contractRepo: ContractDBDAO,
                                 logDao: LogDao,
                                 val reactiveMongoApi: ReactiveMongoApi,
                                 implicit val configuration: Configuration

                                ) extends AbstractController(components)
  with MongoController with ReactiveMongoComponents with TraceUtils {


  def status: Action[AnyContent] = Action { request =>
    entering("status")

    ContractsManager(configuration, contractRepo, logDao).initProjectsDirectories()
    // creation company pour super admin
    //recupérer un user admin

    userDao.count().map(entries => {
      if (entries == 0) {
        entering("start")
        val companyInput = CompanyInput(
          siret = configuration.get[String]("admin.company.siret")
        )
        companiesManager.createCompany(companyInput)
          .map(
            company => {
              val adminInput = UserInput(
                name = configuration.get[String]("admin.user.name"),
                familyName = configuration.get[String]("admin.user.familyName"),
                telephone = configuration.get[String]("admin.user.telephone"),
                email = configuration.get[String]("admin.user.email"),
                password = configuration.get[String]("admin.user.password"),
                roles = List("ADMIN", "USER"),
                companyId = company._id.get
              )
              userManager.createUser(adminInput)
            }
          )
      }
    })
    Ok(
      s"""
         |{
         |"status":"❤",
         |${BuildInfo.toString},
         |"bdd-connection":${reactiveMongoApi.connection.active},
         |"user-header":${request.headers.get("USER-ID").toString},
         |}""".stripMargin)
  }

}
