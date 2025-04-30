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

package smart_contracts.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Abi(
                _id: Option[String],
                name: String,
                blockchain: String,
                abi: String,
                address: String,
                userId: String
              )

object Abi {
  implicit val handler: BSONDocumentHandler[Abi] = Macros.handler[Abi]
  implicit val format: OFormat[Abi] = Json.format[Abi]
}

