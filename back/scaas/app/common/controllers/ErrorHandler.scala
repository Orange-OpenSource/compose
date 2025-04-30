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

import common.exceptions._
import common.utils.TraceUtils
import play.api.libs.json._
import play.api.mvc.Result
import play.api.mvc.Results._

import scala.concurrent.Future

trait ErrorHandler extends TraceUtils {

  def handleError(methodName: String, exception: Exception): Result = {
    exception match {
      case BadRequestException(e) =>
        error(methodName, e)
        BadRequest(
          Json.obj(
            "code" -> 400,
            "message" -> "Bad request"
          )
        )
      case ConflictException(e) =>
        error(methodName, e)
        Conflict(
          Json.obj(
            "code" -> 409,
            "message" -> s"Conflict $e"
          )
        )
      case BlockchainException(e) =>
        error(methodName, e)
        Status(421)(
          Json.obj(
            "code" -> 421,
            "message" -> "Blockchain error"
          )
        )
      case DAOException(e) =>
        error(methodName, e)
        InternalServerError(
          Json.obj(
            "code" -> 500,
            "message" -> "Dao Exception"
          )
        )
      case NotReadyException(e) =>
        error(methodName, e)
        Status(425)(
          Json.obj(
            "code" -> 425,
            "message" -> e
          )
        )
      case ParseException(e) =>
        error(methodName, e)
        BadRequest(
          Json.obj(
            "code" -> 400,
            "message" -> "Parsing error"
          )
        )
      case AuthException(e) =>
        error(methodName, e)
        Forbidden(
          Json.obj(
            "code" -> 403,
            "message" -> "AuthException"
          )
        )
      case LoginException(e) =>
        error(methodName, e)
        Forbidden(
          Json.obj(
            "code" -> 403,
            "message" -> "Wrong login/password"
          )
        )
      case _ =>
        error(methodName, exception.toString)
        InternalServerError(
          Json.obj(
            "code" -> 500,
            "message" -> "Internal server error"
          )
        )
    }
  }

  def handleValidationErrors(errors: scala.collection.Seq[(JsPath, scala.collection.Seq[JsonValidationError])]): Future[Result] = {
    val errorMessages = errors.flatMap {
      case (path, validationErrors) =>
        validationErrors.map(error => s"$path: ${error.message}")
    }
    val errorMessage = s"Invalid element format: ${errorMessages.mkString(", ")}"
    Future.successful(
      BadRequest(
        Json.obj(
          "code" -> 400,
          "message" -> errorMessage
        )
      )
    )
  }
}
