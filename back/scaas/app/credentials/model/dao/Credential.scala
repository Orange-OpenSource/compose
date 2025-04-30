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

package credentials.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Credential(
                       _id: Option[String],
                       salt: String,
                       hashedPassword: String,
                       otp: Option[String]
                     )

object Credential {
  implicit val handler: BSONDocumentHandler[Credential] = Macros.handler[Credential]
  implicit val format: OFormat[Credential] = Json.format[Credential]
}
