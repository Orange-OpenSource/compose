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

package wallets.controllers

import org.web3j.crypto.WalletFile
import play.api.Configuration
import smart_contracts.model.blockchain.KeyPair
import wallets.model.dao.Wallet

trait WalletManager {

  def generateWallet(name: String, userPassword: String, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet

  def loadWallet(walletContent: String): WalletFile

  def importWallet(name: String, userPassword: String, keyPair: KeyPair, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet

  def getKeyPair(walletContent: String, walletPassword: String): KeyPair
}

