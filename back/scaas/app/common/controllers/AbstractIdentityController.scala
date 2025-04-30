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

import common.dao.AbstractDao
import login.dao.TokensDao
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._
import play.api.mvc._
import users.dao.UserDao

import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.reflect.ClassTag

abstract class AbstractIdentityController[T: ClassTag] @Inject()(cc: ControllerComponents,
                                                                 dao: AbstractDao[T],
                                                                 userDao: UserDao,
                                                                 tokensDao: TokensDao,
                                                                 ec: ExecutionContext,
                                                                 implicit val format: Format[T]) extends AbstractController(cc) with ErrorHandler {
  protected val auth: AuthorizedWithSIU = AuthorizedWithSIU(userDao, tokensDao, cc, ec)

  val sanitize: T => JsValue

  def count: Action[AnyContent] = auth.authorized(List("USER")).async {
    entering("count")
    dao.count().map { nb =>
      Ok(Json.toJson(nb))
    }.recover {
      case exception: Exception =>
        handleError("count", exception)
    }
  }

  def getAll: Action[AnyContent] = auth.authorized(List("ADMIN")).async {
    entering("getAll")
    dao.findAll().map { elements =>
      Ok(Json.toJson(elements.map(sanitize)))
    }.recover {
      case exception: Exception =>
        handleError("getAll", exception)
    }
  }

  def getOne(elementId: String): Action[AnyContent] = auth.authorized(List("USER")).async { _ =>
    entering("getOne")
    dao.findById(elementId).map { element =>
      Ok(sanitize(element))
    }.recover {
      case exception: Exception =>
        handleError("getOne", exception)
    }
  }

  def create(): Action[JsValue] = auth.authorized(List("USER")).async(parse.json) {
    entering("create")
    _.body.validate[T](format).fold(
      errors => handleValidationErrors(errors),
      element => {
        val modifiedElement = modifyElementWithId(element, randomUUID.toString)
        dao.create(modifiedElement: T).map { _ =>
          Created(sanitize(modifiedElement))
        }.recover {
          case exception: Exception =>
            handleError("create", exception)
        }
      }
    )
  }

  def update(elementId: String): Action[JsValue] = auth.authorized(List("USER")).async(parse.json) { req =>
    entering("update")
    req.body.validate[T](format).fold(
      errors => handleValidationErrors(errors),
      element => {
        val updatedElement = modifyElementWithId(element, elementId)
        val clazz = implicitly[ClassTag[T]].runtimeClass
        dao.updateById(elementId, updatedElement).map {
          case element: T if clazz.isInstance(element) =>
            Ok(sanitize(element))
          case _ => NotFound(s"Element with ID $elementId not found")
        }.recover {
          case exception: Exception =>
            handleError("update", exception)
        }
      }
    )
  }

  def delete(elementId: String): Action[AnyContent] = auth.authorized(List("ADMIN")).async { _ =>
    entering("delete")
    dao.deleteById(elementId).map {
      _ => NoContent
    }.recover {
      case exception: Exception =>
        handleError("delete", exception)
    }
  }

  private def modifyElementWithId(element: T, id: String): T = {
    val elementJson = Json.toJson[T](element)(format)
    val modifiedJson = elementJson.as[JsObject] + ("_id" -> JsString(id))
    modifiedJson.as[T](format)
  }
}

