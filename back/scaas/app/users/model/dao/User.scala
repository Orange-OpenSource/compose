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

package users.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class User(
                 _id: Option[String],
                 name: String,
                 familyName: String,
                 email: String,
                 telephone: String,
                 credential: String,
                 machines: List[String],
                 roles: List[String],
                 companyId: String
               )

object User {
  implicit val handler: BSONDocumentHandler[User] = Macros.handler[User]
  implicit val format: OFormat[User] = Json.format[User]
}
