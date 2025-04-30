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

package wallets.controllers.factory

import common.exceptions.ParseException
import common.utils.TraceUtils
import play.api.Configuration
import wallets.controllers.WalletManager
import wallets.controllers.ethereum.EthereumWalletManager

class WalletFactory extends TraceUtils {
  def build(configuration: Configuration, blockchainName: String): WalletManager = {

    configuration.get[String](s"blockchain.$blockchainName.type") match {
      case "ethereum" => EthereumWalletManager()
      case "ethereumlegacy" => EthereumWalletManager()
      case _ => throw ParseException("Blockchain type does not exist")
    }

  }

}
