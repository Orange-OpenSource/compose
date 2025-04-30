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
import common.utils.TraceUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName}
import smart_contracts.controllers.blockchain.EventManager
import smart_contracts.controllers.blockchain.ethereum.EthereumEventManager.{PARAMS_ADDRESS, PARAMS_SINCE}
import smart_contracts.controllers.vm.evm.{EvmEventDecoder, EvmSmartContract}
import smart_contracts.dao.AbiDao
import smart_contracts.model.blockchain.DecodedEvent

import java.math.BigInteger
import scala.concurrent.{ExecutionContext, Future}
import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.ListHasAsScala

class EthereumEventManager(abiDao: AbiDao, blockchain: String, web3j: Web3j, implicit val executionContext: ExecutionContext) extends EventManager(blockchain) with TraceUtils {

  override def decode(log: Any, address: String): Future[DecodedEvent] = {
    entering("decode(Any, address: String)")


    log match {
      case _: Log =>
        debug("Log", s" le log match $log")

        for {
          smartContract <- abiDao.findOneBy("address")(address).map(EvmSmartContract(_))
          decodedEvent <- Future.fromTry(EvmEventDecoder(smartContract).decode(log))

        } yield decodedEvent

      case _ => Future.failed(BlockchainException(s"$log is not an ethereum log"))
    }


  }

  override def getEvents(params: Map[String, String]): Future[Seq[DecodedEvent]] = {
    val filter: EthFilter = new EthFilter(
      DefaultBlockParameter.valueOf(new BigInteger(params(PARAMS_SINCE), 10)),
      DefaultBlockParameterName.LATEST, params(PARAMS_ADDRESS)
    )
    Future.sequence(
      web3j
        .ethGetLogs(filter)
        .send()
        .getLogs
        .asScala
        .map(_.get().asInstanceOf[Log])
        .map(log => decode(log, params(PARAMS_ADDRESS))).toList)

  }
}

object EthereumEventManager {
  val PARAMS_ADDRESS = "address"
  val PARAMS_SINCE = "since"
  val PARAMS_TOPIC0 = "topic0"
  val PARAMS_TOPIC1 = "topic1"
  val PARAMS_TOPIC2 = "topic2"
  val PARAMS_TOPIC3 = "topic3"

  def apply(abiDao: AbiDao, blockchain: String, web3j: Web3j, executionContext: ExecutionContext): EthereumEventManager = new EthereumEventManager(abiDao, blockchain, web3j, executionContext)
}
