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

package applications.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Application(
                        _id: Option[String],
                        name: String,
                        ipAddr: Option[String],
                        hashedApiKey: String,
                        salt: String,
                        blockchain: String,
                        walletId: String
                      )

object Application {
  implicit val handler: BSONDocumentHandler[Application] = Macros.handler[Application]
  implicit val format: OFormat[Application] = Json.format[Application]
}
