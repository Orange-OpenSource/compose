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

package companies.model.body

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class CompanyInput(
                         siret: String
                       )

object CompanyInput {
  implicit val handler: BSONDocumentHandler[CompanyInput] = Macros.handler[CompanyInput]
  implicit val format: OFormat[CompanyInput] = Json.format[CompanyInput]
}
