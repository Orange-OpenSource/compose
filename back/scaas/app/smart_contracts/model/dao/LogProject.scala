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

case class LogProject(
                       _id: String,
                       projectId: String,
                       method: String,
                       timestamp: String,
                       log: String
                     )

object LogProject {
  implicit val handler: BSONDocumentHandler[LogProject] = Macros.handler[LogProject]
  implicit val format: OFormat[LogProject] = Json.format[LogProject]
}
