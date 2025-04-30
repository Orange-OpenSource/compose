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
import smart_contracts.model.blockchain.evm.Parameter

case class FunctionParameters(options: Option[TransactionOptions], walletPassword: String, tokenValue  : Option[BigInt] , parameters: List[Parameter])

object FunctionParameters {
  implicit val format: OFormat[FunctionParameters] = Json.format[FunctionParameters]
}
