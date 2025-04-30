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

package smart_contracts.controllers

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.utils.TraceUtils
import login.dao.TokensDao
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import smart_contracts.dao.LogDao
import smart_contracts.model.dao.LogProject
import users.dao.UserDao

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LogController @Inject()(
                               logDao: LogDao,
                               userDao: UserDao,
                               tokensDao: TokensDao,
                               implicit val ec: ExecutionContext,
                               cc: ControllerComponents
                             ) extends AbstractIdentityController[LogProject](cc, logDao, userDao, tokensDao, ec, LogProject.format) with TraceUtils {

  override val sanitize: LogProject => JsValue = JsonSanitizer()("_id")(LogProject.format)


  def getLogByProjectId(userId: String, projectId: String): Action[AnyContent] = auth.authorized(List("USER")).async { _ =>
    entering("getLogByProjectId")

    logDao.findBy("projectId")(projectId).map(logs => Ok(Json.toJson(logs.filter(log => log.projectId == projectId).map(sanitize))))
      .recover {
        case e: Exception => handleError("getLogByProjectId", e)
      }
  }

  def getLogByProjectIdAndMethod(userId: String, projectId: String, method: String): Action[AnyContent] = auth.authorized(List("USER")).async { _ =>
    entering("getLogByProjectIdAndMethod")

    logDao.findBy("projectId")(projectId).map(logs => Ok(Json.toJson(logs.filter(log => log.method == method).map(sanitize))))
      .recover {
        case e: Exception => handleError("getLogByProjectIdAndMethod", e)
      }
  }

  def getLogByTimestamp(userId: String, timestamp: String): Action[AnyContent] = auth.authorized(List("USER")).async { _ =>
    entering("getLogByDate")

    logDao.findBy("timestamp")(timestamp).map(logs => Ok(Json.toJson(logs.filter(log => log.timestamp == timestamp).map(sanitize))))
      .recover {
        case e: Exception => handleError("getLogByDate", e)
      }
  }

  def getLogByDate(userId: String, since: String, until: String): Action[AnyContent] = auth.authorized(List("USER")).async { _ =>
    entering("getLogByDate")

    logDao.findAll().map(logs => Ok(Json.toJson(logs.filter(log => log.timestamp >= since && log.timestamp <= until).map(sanitize))))
      .recover {
        case e: Exception => handleError("getLogByDate", e)
      }
  }
}
