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

package wallets.model.dao

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class Wallet(_id: Option[String], address: String, name: String, blockchain: String, salt: String, file: String, userId: String)

object Wallet {
  implicit val handler: BSONDocumentHandler[Wallet] = Macros.handler[Wallet]
  implicit val format: OFormat[Wallet] = Json.format[Wallet]
}
