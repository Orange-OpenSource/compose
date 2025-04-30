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

package wallets.model.body

import play.api.libs.json.{Json, OFormat}


case class ConfigWallet(name: String, password: String, blockchain: String)

object ConfigWallet {
  implicit val format: OFormat[ConfigWallet] = Json.format[ConfigWallet]
}



