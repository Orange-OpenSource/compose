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

import credentials.dao.CredentialDao
import credentials.model.body.CredentialInput
import credentials.model.dao.Credential
import org.mindrot.jbcrypt.BCrypt
import users.dao.UserDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CredentialsManager @Inject()(
                                    credentialRepo: CredentialDao,
                                    userDao: UserDao
                                  ) {
  def createFromPassword(password: String): Future[Credential] = {
    val salt: String = BCrypt.gensalt()
    val passwordSeq = password.asInstanceOf[CharSequence]
    val passwordRegex = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%!]).{8,}$""".r // Au moins huit caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial

    if (passwordRegex.findFirstMatchIn(passwordSeq).isDefined) {
      val hashedPassword = BCrypt.hashpw(passwordSeq.toString, salt)

      val newCredential = Credential(
        _id = Some(randomUUID.toString),
        salt = salt,
        hashedPassword = hashedPassword,
        otp = Some("")
      )

      credentialRepo.create(newCredential: Credential).map(_ => newCredential)
    } else {
      Future.failed(new Exception("Invalid credential format"))
    }
  }

  def updateCredential(userId: String, credential: CredentialInput): Future[Credential] = {
    userDao.findById(userId).flatMap({
      case user =>
        credentialRepo.findById(user.credential).flatMap { daoCredential =>
          val passwordSeq = credential.password.asInstanceOf[CharSequence]
          val passwordRegex = """^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@#$%!]).{8,}$""".r // Au moins huit caractères, dont une majuscule, une minuscule, un chiffre et un caractère spécial

          if (passwordRegex.findFirstMatchIn(passwordSeq).isDefined) {
            val hashedPassword = BCrypt.hashpw(passwordSeq.toString, daoCredential.salt)
            val updatedCredential = Credential(
              daoCredential._id,
              daoCredential.salt,
              hashedPassword = hashedPassword,
              credential.otp
            )

            credentialRepo.updateById(user.credential, updatedCredential)
          } else {
            Future.failed(new Exception("Invalid credential format"))
          }
        }
      case null => Future.failed(new Exception("User ID not found"))
    })
  }
}
