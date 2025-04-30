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


/*
private 
 */
case class Block(number: Option[String],
                 hash: Option[String],
                 parentHash: Option[String],
                 nonce: Option[String],
                 sha3Uncles: Option[String],
                 logsBloom: Option[String],
                 transactionsRoot: Option[String],
                 stateRoot: Option[String],
                 receiptsRoot: Option[String],
                 author: Option[String],
                 miner: Option[String],
                 mixHash: Option[String],
                 difficulty: Option[String],
                 totalDifficulty: Option[String],
                 extraData: Option[String],
                 size: Option[String],
                 gasLimit: Option[String],
                 gasUsed: Option[String],
                 timestamp: Option[String],
                 uncles: Option[List[String]],
                 sealFields: Option[List[String]],
                 baseFeePerGas: Option[String])


object Block {
  implicit val format: OFormat[Block] = Json.format[Block]
}
