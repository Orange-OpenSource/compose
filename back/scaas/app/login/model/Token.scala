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

package login.model

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Token(_id: Option[String], userId: String, userToken: String, expirationDate: Long)

object Token {
  implicit val handler: BSONDocumentHandler[Token] = Macros.handler[Token]
  implicit val format: OFormat[Token] = Json.format[Token]


}
