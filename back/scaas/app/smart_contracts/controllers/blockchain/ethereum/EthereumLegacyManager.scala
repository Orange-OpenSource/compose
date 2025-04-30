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

package smart_contracts.controllers.blockchain.ethereum

import common.exceptions.BlockchainException
import org.web3j.crypto.{Credentials, Keys, RawTransaction, TransactionEncoder}
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.response.EthSendTransaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import play.api.Configuration

import java.math.BigInteger
import scala.util.{Failure, Success, Try}
class EthereumLegacyManager(override val blockchainName: String,
                            configuration: Configuration,
                            gasLimit: BigInt = BigInt("4042055464", 10),
                            gasPremium: BigInt = BigInt("2500000000", 10),
                            feeCap: BigInt = BigInt("4042055464", 10)
)(web3j: Web3j = Web3j.build(new HttpService(configuration.get[String](s"blockchain.$blockchainName.url"))))extends EthereumManager(blockchainName,configuration, gasLimit, gasPremium, feeCap)(){


  override def invoke(encodedFunction: String, tokenValue:BigInt, contractAddress: String)(credentials: Credentials): Try[String] = {
    entering("invoke")

    val fromAddress = Keys.toChecksumAddress(credentials.getAddress)
    val tx = org.web3j.protocol.core.methods.request.Transaction.createFunctionCallTransaction(
      fromAddress,
      nonce(credentials),
      web3j.ethGasPrice.send.getGasPrice,
      gasLimit.bigInteger,
      contractAddress,
      encodedFunction)

    val estimate = web3j.ethEstimateGas(tx).send().getAmountUsed.multiply(new BigInteger("2", 10))

    val rawTransaction = RawTransaction.createTransaction(
      nonce(credentials), //nonce
      BigInt(0).bigInteger, // gasprice
      BigInt(2).bigInteger.multiply(estimate), //gaslimit
      contractAddress, //to
      tokenValue.bigInteger, //value
      encodedFunction //data
    )
    debug("raw transaction type : ",rawTransaction.getType.toString)
    sendRawTransaction(rawTransaction)(credentials)
  }



  override def nonce(credentials: Credentials): BigInteger = web3j.ethGetTransactionCount(credentials.getAddress, DefaultBlockParameterName.LATEST).send().getTransactionCount

  override def sendRawTransaction(rawTransaction: RawTransaction)(credentials: Credentials): Try[String] = {
    val signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials)

    val hexValue = Numeric.toHexString(signedMessage)

    // send the transactions
    val ethSendTransaction: EthSendTransaction = web3j.ethSendRawTransaction(hexValue).send()
    // check that is has been sent correctly
    val transactionHash = ethSendTransaction.getTransactionHash
    if (transactionHash == null) {
      return Failure(BlockchainException("Unable to execute function: " + ethSendTransaction.getError.getMessage))
    }

    debug("sendRawTransaction", s"Raw transaction has been sent with hash [$transactionHash]")

    Success(transactionHash)
  }

}
object EthereumLegacyManager {
  def apply(blockchainName: String, configuration: Configuration, gasLimit: BigInt, gasPremium: BigInt, feeCap: BigInt): EthereumLegacyManager = new EthereumLegacyManager(blockchainName, configuration, gasLimit, gasPremium, feeCap)()

  def apply(blockchainName: String, configuration: Configuration): EthereumLegacyManager = new EthereumLegacyManager(blockchainName, configuration)()
}
