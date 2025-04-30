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


case class Transaction(transactionHash: Option[String],
                       transactionIndex: Option[String],
                       blockHash: Option[String],
                       blockNumber: Option[String],
                       cumulativeGasUsed: Option[String],
                       gasUsed: Option[String],
                       contractAddress: Option[String],
                       root: Option[String],
                       // status is only present on Byzantium transactions onwards
                       // see EIP 658 https://github.com/ethereum/EIPs/pull/658
                       status: Option[String],
                       from: Option[String],
                       to: Option[String],
                       logs: Option[List[Log]],
                       logsBloom: Option[String],
                       revertReason: Option[String],
                       `type`: Option[String],
                       effectiveGasPrice: Option[String])

object Transaction {
  implicit val format: OFormat[Transaction] = Json.format[Transaction]
}
