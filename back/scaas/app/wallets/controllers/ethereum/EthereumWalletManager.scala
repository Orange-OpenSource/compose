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

package wallets.controllers.ethereum

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import common.utils.{CryptoUtils, TraceUtils}
import org.web3j.crypto.{ECKeyPair, Keys, WalletFile}
import play.api.Configuration
import smart_contracts.model.blockchain.KeyPair
import wallets.controllers.WalletManager
import wallets.model.dao.Wallet
import wallets.utils.EthereumWalletUtils

import java.io.StringWriter
import java.util.UUID.{randomUUID => genId}

class EthereumWalletManager @Inject()(walletUtils: EthereumWalletUtils = EthereumWalletUtils()) extends WalletManager with TraceUtils {

  private val objectMapper = new ObjectMapper()

  objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true)

  def generateWallet(name: String, userPassword: String, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet = {
    entering("generateTestableWallet")
    val ecKeyPair = Keys.createEcKeyPair()
    val keyPair = KeyPair(ecKeyPair.getPrivateKey, ecKeyPair.getPublicKey)
    importWallet(name, userPassword, keyPair, blockchain, userId)
  }

  def importWallet(name: String, userPassword: String, keyPair: KeyPair, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet = {
    entering("importTestableWallet")

    val cryptool = CryptoUtils(configuration)
    val salt = cryptool.generateSafeToken(16)
    val password = cryptool.retrieveWalletKey(salt, userPassword)

    val walletFile = walletUtils.createWallet(password, new ECKeyPair(keyPair.privateKey.bigInteger, keyPair.publicKey.bigInteger))
    val id = Some(genId().toString)
    val out = new StringWriter
    objectMapper.writeValue(out, walletFile)
    Wallet(id, Keys.toChecksumAddress(walletFile.getAddress), name, blockchain, salt, out.toString, userId)
  }

  def loadWallet(walletContent: String): WalletFile = objectMapper.readValue(walletContent, classOf[WalletFile])

  override def getKeyPair(walletContent: String, walletPassword: String): KeyPair = {

    val walletFile = loadWallet(walletContent)
    val credentials = walletUtils.loadCredentials(walletPassword, walletFile)
    val eCKeyPair = credentials.getEcKeyPair

    KeyPair(eCKeyPair.getPrivateKey, eCKeyPair.getPublicKey)
  }

}

object EthereumWalletManager {
  def apply(): EthereumWalletManager = new EthereumWalletManager()
}
