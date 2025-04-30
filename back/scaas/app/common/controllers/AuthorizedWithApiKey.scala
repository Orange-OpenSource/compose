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

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.utils.TraceUtils
import org.mindrot.jbcrypt.BCrypt
import play.api.libs.json.Json
import play.api.mvc.ControllerComponents
import play.api.mvc.Results.Forbidden
import play.api.mvc.Security.AuthenticatedBuilder

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AuthorizedWithApiKey(machineDBDAO: ApplicationDao,
                           cc: ControllerComponents,
                           implicit val ec: ExecutionContext) extends TraceUtils {

  def authorized: AuthenticatedBuilder[Application] = new AuthenticatedBuilder(

    userinfo = r => {
      val id = r.headers.get("client-id")
      val apiKey = r.headers.get("api-key")

      val f: Future[Option[Application]] = id match {
        case Some(id) =>
          machineDBDAO.
            findById(id)
            .map(machine => {
              debug("bcrypt = ", BCrypt.hashpw(apiKey.get, machine.salt))
              debug("apikey = ", machine.hashedApiKey)
              debug("ip", machine.ipAddr.getOrElse(r.connection.remoteAddressString))
              debug("ip2", r.connection.remoteAddressString)
              if (BCrypt.hashpw(apiKey.get, machine.salt) == machine.hashedApiKey && machine.ipAddr.getOrElse(r.connection.remoteAddressString) == r.connection.remoteAddressString) {
                Some(machine)
              } else None
            }).recover {
            case _ => None
          }
        case None => Future.successful(None)
      }
      Await.result(f, Duration.Inf)
    },
    defaultParser = cc.parsers.anyContent,
    onUnauthorized = _ => Forbidden(
      Json.obj(
        "code" -> 403,
        "message" -> "you are not authorized to access this resource."
      )
    )
  )

}

object AuthorizedWithApiKey {
  def apply(machineDBDAO: ApplicationDao, cc: ControllerComponents, ec: ExecutionContext): AuthorizedWithApiKey = new AuthorizedWithApiKey(machineDBDAO, cc, ec)
}
