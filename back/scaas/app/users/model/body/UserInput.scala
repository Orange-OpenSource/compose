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

package users.model.body

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class UserInput(
                      name: String,
                      familyName: String,
                      telephone: String,
                      email: String,
                      password: String,
                      roles: List[String],
                      companyId: String
                    )

object UserInput {
  implicit val handler: BSONDocumentHandler[UserInput] = Macros.handler[UserInput]
  implicit val format: OFormat[UserInput] = Json.format[UserInput]
}
