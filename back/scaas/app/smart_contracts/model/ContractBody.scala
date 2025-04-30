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

package smart_contracts.model

import play.api.libs.json._
import smart_contracts.model.body.WalletInfos
import smart_contracts.model.dao.ContractOptions

case class ContractBody(wallet: WalletInfos,
                        options: ContractOptions,
                        migration: List[ContractInstructions]
                       )

object ContractBody {
  implicit val format: OFormat[ContractBody] = Json.format[ContractBody]
}
