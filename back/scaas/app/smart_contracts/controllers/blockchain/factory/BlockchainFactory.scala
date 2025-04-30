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

package smart_contracts.controllers.blockchain.factory

import common.exceptions.ParseException
import play.api.Configuration
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.controllers.blockchain.ethereum.{EthereumLegacyManager, EthereumManager}

class BlockchainFactory {
  def build(blockchainType : String, configuration: Configuration, blockchainName : String ):BlockchainManager={
    blockchainType match {
      case "ethereum" => EthereumManager(blockchainName,configuration)
      case "ethereumlegacy" => EthereumLegacyManager(blockchainName,configuration)
      case _ => throw ParseException("Blockchain type does not exist")
    }

  }
}
