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

case class UserInputUpdate(
                            name: Option[String] = None,
                            familyName: Option[String] = None,
                            telephone: Option[String] = None,
                            roles: Option[List[String]] = None
                          )

object UserInputUpdate {
  implicit val handler: BSONDocumentHandler[UserInputUpdate] = Macros.handler[UserInputUpdate]
  implicit val format: OFormat[UserInputUpdate] = Json.format[UserInputUpdate]
}
