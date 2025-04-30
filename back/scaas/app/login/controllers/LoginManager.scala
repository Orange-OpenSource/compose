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

import common.exceptions.AuthException
import common.utils.CryptoUtils
import credentials.dao.CredentialDao
import login.dao.TokensDao
import login.model.{LoginInput, Token}
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import play.api.Configuration
import users.dao.UserDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LoginManager @Inject()(
                              credentialDao: CredentialDao,
                              userDao: UserDao,
                              tokensDao: TokensDao,
                              configuration: Configuration,
                              implicit val executionContext: ExecutionContext
                            ) {

  def isLoginOk(loginInput: LoginInput): Future[Token] = {
    userDao.findOneBy("email")(loginInput.email).flatMap(
      user => {
        credentialDao.findById(user.credential).flatMap(c => {
          if (BCrypt.hashpw(loginInput.password, c.salt) == c.hashedPassword) {
            val cryptool = CryptoUtils(configuration)
            val expirationTimestamp = DateTime.now().plusMinutes(90)
            val token = Token(Some(randomUUID.toString),user._id.get, cryptool.generateSafeToken(24), expirationTimestamp.getMillis)
            tokensDao
              .create(token)
              .map(token => {
                token
              })
          } else {
            throw AuthException("Invalid password")
          }
        }
        )
      }
    )
  }

}
