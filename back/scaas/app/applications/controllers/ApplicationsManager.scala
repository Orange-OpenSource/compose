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
import common.utils.{CryptoUtils, TraceUtils}
import org.mindrot.jbcrypt.BCrypt
import play.api.Configuration
import users.dao.UserDao
import users.model.dao.User
import wallets.dao.WalletDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class ApplicationsManager @Inject()(
                                     configuration: Configuration,
                                     walletDao: WalletDao,
                                     machineRepo: ApplicationDao,
                                     userDao: UserDao
                                   ) extends TraceUtils{
  val cryptoTool: CryptoUtils = CryptoUtils(configuration)
  private val tokenSize = 20

  def createMachine(userId: String, blockchain: String, machine: ApplicationInput): Future[(String, String)] = {
    entering("createMachine")
    val salt: String = BCrypt.gensalt()
    val apiKey: String = cryptoTool.generateSafeToken(tokenSize)
    val hashedApiKey: String = BCrypt.hashpw(apiKey, salt)
    val applicationId: String = randomUUID.toString
    walletDao.findOneByAddress(userId, blockchain, machine.walletAddress).flatMap(wallet => {
      debug("createmachine",wallet.toString)
      val newMachine: Application = Application(
        Some(applicationId),
        machine.name,
        machine.ipAddr,
        hashedApiKey,
        salt,
        blockchain,
        wallet._id.get
      )
      userDao.findById(userId).map(user => {
        debug("createmahcine", user.toString)
        val updatedUser: User = User(
          _id = user._id,
          name = user.name,
          familyName = user.familyName,
          email = user.email,
          telephone = user.telephone,
          credential = user.credential,
          machines = user.machines :+ newMachine._id.get,
          roles = user.roles,
          companyId = user.companyId
        )
        userDao.updateById(userId, updatedUser)
      })
      debug("createmachine", newMachine.toString)
      machineRepo.create(newMachine: Application).map(_ => (apiKey, applicationId))
    })
  }

}
