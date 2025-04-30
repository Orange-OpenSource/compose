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

import common.exceptions.AuthException
import common.utils.TraceUtils
import login.dao.TokensDao
import org.joda.time.DateTime
import play.api.libs.json.Json
import play.api.mvc.Results.Forbidden
import play.api.mvc.Security.AuthenticatedBuilder
import play.api.mvc.{ControllerComponents, RequestHeader}
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class AuthorizedWithSIU(userDAO: UserDao,
                        tokensDao: TokensDao,
                        cc: ControllerComponents,
                        implicit val ec: ExecutionContext) extends TraceUtils {

  def authorized(roles: List[String]): AuthenticatedBuilder[User] = new AuthenticatedBuilder(

    userinfo = r => {
      entering("authorized")
      val id: Option[String] = r.headers.get("user-id")
      val urlId: String = getUserIdFromUrl(r)
      val token: String = r.headers.get("user-token").getOrElse("")


      def idOptionToFuture[T](id: Option[T]): Future[T] =
        id match {
          case Some(u) => Future.successful(u)
          case None => Future.failed(AuthException("id not provided"))
        }

      val futureTuple = for {
        idString <- idOptionToFuture[String](id)
        user <- userDAO.findById(idString)
        token <- tokensDao.findOneBy("userToken")(token)

      } yield (user, token)

      val futureUser = futureTuple.flatMap(tuple => {
        val user = tuple._1
        val token = tuple._2
        if (token.userId == user._id.get && token.expirationDate > DateTime.now().getMillis && roleOk(roles, user.roles)) {
          prepareUser(roles, user, urlId)
        } else {
          Future.failed(AuthException("you are not authorized to access this resource."))
        }
      })
      val res = Await.result(futureUser, Duration.Inf)
      Option(res)
    },
    defaultParser = cc.parsers.anyContent,
    onUnauthorized = _ => Forbidden(
      Json.obj(
        "code" -> 403,
        "message" -> "you are not authorized to access this resource."
      )
    )
  )

  private def prepareUser(roles: List[String], user: User, urlId: String): Future[User] = {

    if (user.roles.contains("SUPER")) {
      // si il est super admin
      Future.successful(user)
    } else if (user.roles.contains("ADMIN")) {
      if (urlId != "noid") {
        // si il est admin et que l'urlid est present on verify que le user appartient a l'entreprise de l'admin
        userDAO.findById(urlId).filter(u => u.companyId == user.companyId)
      } else {
        Future.successful(user)
      }
    } else if (user.roles.contains("USER")) {
      if (urlId != "noid") {
        //si c'est un USER, on vérifie que l'urlId est bien le même que le userId
        if (urlId == user._id.get) {
          Future.successful(user)
        } else {
          Future.failed(AuthException("You are not authorized to access this resource."))
        }
      } else {
        Future.successful(user)
      }

    } else {
      Future.failed(AuthException("You are not authorized to access this resource."))
    }

  }

  private def roleOk(roles: List[String], userRoles: List[String]): Boolean = {
    entering("roleOk")
    var bool: Boolean = false
    for (r <- userRoles; if !bool) {
      if (roles.contains(r)) {
        bool = true
      } else {
        bool = false
      }
    }
    bool
  }

  private def getUserIdFromUrl(r: RequestHeader): String = {
    entering("getUserIdFromUrl")
    val tab = r.toString().split("/")
    var userid = "noid"
    for (e <- tab) {
      if (e == "users" && tab.indexOf(e) < tab.length - 1) {
        userid = tab.apply(tab.indexOf(e) + 1)
      }
    }
    userid
  }
}

object AuthorizedWithSIU {
  def apply(userDAO: UserDao, tokensDao: TokensDao, cc: ControllerComponents, ec: ExecutionContext): AuthorizedWithSIU = new AuthorizedWithSIU(userDAO, tokensDao, cc, ec)
}
