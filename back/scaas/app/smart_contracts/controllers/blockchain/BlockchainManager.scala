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

package smart_contracts.controllers.blockchain

import org.web3j.crypto.Credentials
import smart_contracts.model.body.{Block, Transaction}

import scala.util.Try


trait BlockchainManager  {
  val blockchainName: String

  // execute call and execute function arr public
  def invokeView(encodedFunction: String, contractAddress: String, senderAddress: String): Try[String]

  def invoke(encodedFunction: String, tokenValue: BigInt,contractAddress: String)(credentials: Credentials): Try[String]

  def transactionReceipt(transactionHash: String): Try[Transaction]



  def getBlockNumber:String

  def getBlockByNumber(number: String): Block

  def getBlockByHash(hash: String): Block

  def getCodeAt(address: String):String

  def getPeersNumber: Int

  def getProtocolVersion:String

  def web3Version(): String
  def getChainId: String

  def getGasPrice: String
}

