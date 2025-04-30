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

case class Contract(
                     _id: Option[String],
                     name: String,
                     blockchain: String,
                     file: String,
                     project: String,
                     owner: String
                   )

object Contract {
  implicit val handler: BSONDocumentHandler[Contract] = Macros.handler[Contract]
  implicit val format: OFormat[Contract] = Json.format[Contract]
}
