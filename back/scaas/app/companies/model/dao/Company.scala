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

package companies.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Company(
                    _id: Option[String],
                    siret: List[String]
                  )

object Company {
  implicit val handler: BSONDocumentHandler[Company] = Macros.handler[Company]
  implicit val format: OFormat[Company] = Json.format[Company]
}
