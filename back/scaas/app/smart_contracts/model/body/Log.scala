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

case class Log(
                removed: Option[Boolean],
                logIndex: Option[String],
                transactionIndex: Option[String],
                transactionHash: Option[String],
                blockHash: Option[String],
                blockNumber: Option[String],
                address: Option[String],
                data: Option[String],
                `type`: Option[String],
                topics: Option[List[String]],
              )

object Log {
  implicit val format: OFormat[Log] = Json.format[Log]
}
