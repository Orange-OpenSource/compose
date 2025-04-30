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

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.controllers.ErrorHandler
import common.exceptions.ConflictException
import credentials.controllers.CredentialsManager
import org.apache.commons.validator.routines.EmailValidator
import users.dao.UserDao
import users.model.body.{UserInput, UserInputUpdate}
import users.model.dao.User

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class UsersManager @Inject()(
                              userRepo: UserDao,
                              machineRepo: ApplicationDao,
                              credentialManager: CredentialsManager
                            ) extends ErrorHandler {

  private val emailValidator = EmailValidator.getInstance()

  def getMachines(userId: String): Future[List[Application]] = {
    userRepo.findById(userId).flatMap { user =>
      val machineFutures: List[Future[Application]] = user.machines.map(machineId => machineRepo.findById(machineId))
      Future.sequence(machineFutures)
    }
  }


  def createUser(user: UserInput): Future[User] = {
    if (emailValidator.isValid(user.email)) {
      userRepo.findBy("email")(user.email).flatMap(

        seqUser => {

          if (seqUser.nonEmpty) {

            Future.failed(ConflictException("email already exist"))
          } else {
            credentialManager.createFromPassword(user.password).flatMap(
              credential => {
                val credentialId: String = credential._id.getOrElse("")
                val newUser: User = User(
                  _id = Some(randomUUID.toString),
                  name = user.name,
                  familyName = user.familyName,
                  email = user.email,
                  telephone = user.telephone,
                  credential = credentialId,
                  machines = List.empty[String],
                  roles = user.roles,
                  companyId = user.companyId
                )
                userRepo.create(newUser: User).map(_ => newUser)
              }
            )
          }
        }
      )


    } else {
      Future.failed(new Exception("Invalid email format"))
    }
  }

  def updateUser(userInput: UserInputUpdate, userId: String): Future[User] = {
    println(userInput)
    userRepo.findById(userId).flatMap({
      user => {
        println(user)
        val updatedUser: User = User(
          _id = user._id,
          name = userInput.name.getOrElse(user.name),
          familyName = userInput.familyName.getOrElse(user.familyName),
          email = user.email,
          telephone = userInput.telephone.getOrElse(user.telephone),
          credential = user.credential,
          machines = user.machines,
          roles = userInput.roles.getOrElse(user.roles),
          companyId = user.companyId
        )

        println(updatedUser)
        userRepo.updateById(userId, updatedUser)

      }
    })

  }
}
