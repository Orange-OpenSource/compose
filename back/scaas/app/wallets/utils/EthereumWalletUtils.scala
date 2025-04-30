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

package wallets.utils

import org.web3j.crypto.{Credentials, ECKeyPair, Wallet, WalletFile}


class EthereumWalletUtils {

  def createWallet(password: String, ecKeyPair: ECKeyPair): WalletFile = Wallet.createStandard(password, ecKeyPair)

  def loadCredentials(password: String, wallet: WalletFile): Credentials = Credentials.create(Wallet.decrypt(password, wallet))
}

object EthereumWalletUtils {
  def apply(): EthereumWalletUtils = new EthereumWalletUtils()
}
