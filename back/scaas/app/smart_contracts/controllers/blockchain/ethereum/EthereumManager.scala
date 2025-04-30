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

import common.exceptions.{BlockchainException, NotReadyException}
import common.utils.TraceUtils
import org.web3j.crypto.{Credentials, Keys, RawTransaction, TransactionEncoder}
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.{EthBlock, EthGetTransactionReceipt, EthSendTransaction, TransactionReceipt}
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName}
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import play.api.Configuration
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.model.body
import smart_contracts.model.body.{Block, Log, Transaction}

import java.math.BigInteger
import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.util.{Failure, Success, Try}

class EthereumManager(val blockchainName: String,
                      configuration: Configuration,
                      gasLimit: BigInt = BigInt("8000000", 10),
                      gasPremium: BigInt = BigInt("2500000000", 10),
                      feeCap: BigInt = BigInt("4042055464", 10)
                     )(web3j: Web3j = Web3j.build(new HttpService(configuration.get[String](s"blockchain.$blockchainName.url")))) extends BlockchainManager with TraceUtils {


  def getBalance(walletAddress: String): String = {
    web3j.ethGetBalance(walletAddress, DefaultBlockParameterName.LATEST).send().getBalance.toString(10)
  }

  def getBlockNumber: String = {
    web3j.ethBlockNumber().send().getBlockNumber.toString(10)
  }

  def getGasPrice: String = {
    web3j.ethGasPrice().send().getGasPrice.toString(10)
  }

  def getChainId: String = {
    web3j.ethChainId().send().getChainId.toString
  }

  def keccak256(data: String): String = {
    web3j.web3Sha3(data).send().getResult
  }

  private def translateBlock(block: EthBlock.Block): Block = {
    Block(
      Try(block.getNumberRaw).toOption, //number: Try[String],
      Try(block.getHash).toOption, //hash: Try[String],
      Try(block.getParentHash).toOption,
      Try(block.getNonceRaw).toOption,
      Try(block.getSha3Uncles).toOption, //parentHash: Try[String],
      Try(block.getLogsBloom).toOption, //nonce: Try[String],
      Try(block.getTransactionsRoot).toOption,
      Try(block.getStateRoot).toOption,
      Try(block.getReceiptsRoot).toOption,
      Try(block.getAuthor).toOption,
      Try(block.getMiner).toOption,
      Try(block.getMixHash).toOption,
      Try(block.getDifficultyRaw).toOption,
      Try(block.getTotalDifficultyRaw).toOption,
      Try(block.getExtraData).toOption,
      Try(block.getSizeRaw).toOption,
      Try(block.getGasLimitRaw).toOption,
      Try(block.getGasUsedRaw).toOption,
      Try(block.getTimestampRaw).toOption,
      Try(block.getUncles.asScala.toList).toOption,
      Try(block.getSealFields.asScala.toList).toOption,
      Try(if (block.getBaseFeePerGas != null) block.getBaseFeePerGas.toString else null).toOption
    )
  }

  def getBlockByHash(hash: String): Block = {
    val block = web3j.ethGetBlockByHash(hash, true).send().getBlock
    translateBlock(block)
  }

  def getBlockByNumber(number: String): Block = {
    val block: EthBlock.Block = web3j.ethGetBlockByNumber(DefaultBlockParameter.valueOf(new BigInteger(number)), true).send().getBlock
    translateBlock(block)

  }

  def getCodeAt(address: String): String = {
    web3j.ethGetCode(address, DefaultBlockParameterName.LATEST).send().getCode
  }

  def getPeersNumber: Int = {
    web3j.adminPeers().send().getResult.size()
  }

  def getProtocolVersion: String = {
    web3j.ethProtocolVersion().send().getProtocolVersion
  }

  def web3Version(): String = {
    web3j.web3ClientVersion().send().getWeb3ClientVersion
  }

  def invokeView(encodedFunction: String, contractAddress: String, userAddress: String = web3j.ethAccounts().send().getAccounts.get(0)): Try[String] = Try {
    entering("invokeView")

    val transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
      userAddress,
      contractAddress,
      encodedFunction
    )

    val ethCall = web3j.ethCall(
        transaction,
        DefaultBlockParameterName.LATEST)
      .send()

    if (ethCall.isReverted) ethCall.getRevertReason
    else ethCall.getValue
  }

  def invoke(encodedFunction: String, tokenValue: BigInt, contractAddress: String)(credentials: Credentials): Try[String] = {
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

    debug("chain ID : ", getChainId.toLong.toString)
    val rawTransaction = RawTransaction.createTransaction(
      getChainId.toLong,
      nonce(credentials),
      gasLimit.bigInteger,
      contractAddress,
      tokenValue.bigInteger,
      encodedFunction,
      estimate,
      new BigInteger("5000000000", 10)
    )


    debug("invoke raw nonce : ", nonce(credentials).toString)

    debug("invoke raw estimate : ", estimate.toString)
    debug("invoke raw contractAddress : ", contractAddress)
    debug("invoke raw tokenValue : ", tokenValue.bigInteger.toString)
    debug("invoke raw encodedFunction : ", encodedFunction)

    debug("raw transaction type : ", rawTransaction.getType.toString)
    debug("Transaction1: ", "  transaction 1")
    sendRawTransaction(rawTransaction)(credentials)

  }

  def transactionReceipt(transactionHash: String): Try[Transaction] = {
    entering("transactionReceipt")
    // get the transaction receipt
    val transactionReceipt: EthGetTransactionReceipt = web3j.ethGetTransactionReceipt(transactionHash).send()
    if (transactionReceipt.getTransactionReceipt.isPresent) {
      val receipt: TransactionReceipt = transactionReceipt.getTransactionReceipt.get()
      if (!receipt.isStatusOK) {
        Failure(BlockchainException(receipt.getRevertReason))
      } else {
        val logs = if (receipt.getLogs != null) {
          Some(receipt.getLogs.asScala.toList.map(log => {
            Log(
              Try(log.isRemoved).toOption,
              Try(log.getLogIndexRaw).toOption,
              Try(log.getTransactionIndexRaw).toOption,
              Try(log.getTransactionHash).toOption,
              Try(log.getBlockHash).toOption,
              Try(log.getBlockNumberRaw).toOption,
              Try(log.getAddress).toOption,
              Try(log.getData).toOption,
              Try(log.getType).toOption,
              Try(log.getTopics.asScala.toList).toOption,
            )
          }))
        } else {
          None
        }

        Success(
          body.Transaction(
            Try(receipt.getTransactionHash).toOption,
            Try(receipt.getTransactionIndexRaw).toOption,
            Try(receipt.getBlockHash).toOption,
            Try(receipt.getBlockNumberRaw).toOption,
            Try(receipt.getCumulativeGasUsedRaw).toOption,
            Try(receipt.getGasUsedRaw).toOption,
            Try(receipt.getContractAddress).toOption,
            Try(receipt.getRoot).toOption,
            Try(receipt.getStatus).toOption,
            Try(receipt.getFrom).toOption,
            Try(receipt.getTo).toOption,
            logs,
            Try(receipt.getLogsBloom).toOption,
            Try(receipt.getRevertReason).toOption,
            Try(receipt.getType).toOption,
            Try(receipt.getEffectiveGasPrice).toOption
          )
        )
      }
    } else {
      Failure(NotReadyException("Transaction does not exist ! "))
    }
  }

  def nonce(credentials: Credentials): BigInteger = web3j.ethGetTransactionCount(Keys.toChecksumAddress(credentials.getAddress), DefaultBlockParameterName.LATEST).send().getTransactionCount

  def sendRawTransaction(rawTransaction: RawTransaction)(credentials: Credentials): Try[String] = {
    val signedMessage = TransactionEncoder.signMessage(rawTransaction, getChainId.toLong, credentials)

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

object EthereumManager {
  def apply(blockchainName: String, configuration: Configuration, gasLimit: BigInt, gasPremium: BigInt, feeCap: BigInt): EthereumManager = new EthereumManager(blockchainName, configuration, gasLimit, gasPremium, feeCap)()

  def apply(blockchainName: String, configuration: Configuration): EthereumManager = new EthereumManager(blockchainName, configuration)()
}
