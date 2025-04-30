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

case class OngoingDeployment(
                              _id: String,
                              blockchain: String,
                              userId: String,
                              updatedAt: String
                            )

object OngoingDeployment {
  implicit val handler: BSONDocumentHandler[OngoingDeployment] = Macros.handler[OngoingDeployment]
  implicit val format: OFormat[OngoingDeployment] = Json.format[OngoingDeployment]
}

