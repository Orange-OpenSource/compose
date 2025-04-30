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

package applications.model.body

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class ApplicationInput(
                             name: String,
                             ipAddr: Option[String],
                             walletAddress: String,
                             blockchain: String
                           )

object ApplicationInput {
  implicit val handler: BSONDocumentHandler[ApplicationInput] = Macros.handler[ApplicationInput]
  implicit val format: OFormat[ApplicationInput] = Json.format[ApplicationInput]
}
