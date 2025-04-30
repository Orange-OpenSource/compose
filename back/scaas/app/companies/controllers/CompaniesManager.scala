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

import companies.dao.CompanyDao
import companies.model.body.CompanyInput
import companies.model.dao.Company
import users.dao.UserDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CompaniesManager @Inject()(
                                  companyRepo: CompanyDao,
                                  userRepo: UserDao,
                                ) {

  def createCompany(company: CompanyInput): Future[Company] = {
    val newCompany: Company = Company(
      _id = Some(randomUUID.toString),
      siret = List(company.siret)
    )
    companyRepo.create(newCompany: Company).map(_ => newCompany)
  }
}
