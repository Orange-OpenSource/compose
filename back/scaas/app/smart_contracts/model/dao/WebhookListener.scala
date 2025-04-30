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

import play.api.libs.json.{Json, OFormat}
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class WebhookListener(_id: String, userId: String, blockchain: String, contractAddress: String, eventName: String, url: String)

object WebhookListener {
  implicit val format: OFormat[WebhookListener] = Json.format[WebhookListener]
  implicit val bsonFormat: BSONDocumentHandler[WebhookListener] = Macros.handler[WebhookListener]

}

