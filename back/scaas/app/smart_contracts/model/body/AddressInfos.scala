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

package smart_contracts.model.body

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class AddressInfos(address: String)

object AddressInfos {
  implicit val format: OFormat[AddressInfos] = Json.format[AddressInfos]
}
