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

case class AbiInput(name: String,
                    abi: String,
                    blockchain: String
                   )


object AbiInput {
  implicit val format: OFormat[AbiInput] = Json.format[AbiInput]
}
