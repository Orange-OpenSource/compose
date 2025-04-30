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


import common.utils.TraceUtils
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import org.web3j.crypto.Keys
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName}
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.{EthBlockNumber, Log}
import org.web3j.protocol.http.HttpService
import play.api.Configuration
import play.api.libs.ws.WSClient
import smart_contracts.controllers.blockchain.{EventListener, EventManager}
import smart_contracts.dao.WebhookListenersDao

import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.concurrent.{ExecutionContext, Future}
import java.math.BigInteger
import scala.concurrent.duration.*
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Read this first
 * https://medium.com/mycrypto/the-ethereum-virtual-machine-how-does-it-work-9abac2b7c9e
 * https://medium.com/mycrypto/understanding-event-logs-on-the-ethereum-blockchain-f4ae7ba50378
 *
 */
class EthereumEventListener(blockchain: String,
                            eventManager: EventManager,
                            webhookListenersDao: WebhookListenersDao,
                            ws: WSClient,
                            configuration: Configuration,
                            override implicit val executionContext: ExecutionContext
                           )(web3j: Web3j = Web3j.build(new HttpService(configuration.get[String](s"blockchain.$blockchain.url")))) extends EventListener(blockchain, eventManager, webhookListenersDao, ws, executionContext) with TraceUtils {

  debug("constructor", s"initializing listener for blockchain [$blockchain] (ethereum listener)")

  given ActorSystem = ActorSystem("SchedulerSystem")
  private val filter = new EthFilter(
    DefaultBlockParameterName.LATEST,
    DefaultBlockParameterName.LATEST,
    List.empty[String].asJava
  ).addNullTopic()
  var blockDeDepart: BigInteger = BigInteger("0")
  
  private val disposableLogFlow: Disposable = {
    debug("constructor", "initializing disposableLogFlow")
    web3j.ethLogFlowable(filter).subscribe((t: Log) => notifyListeners(t, Keys.toChecksumAddress(t.getAddress)), new Consumer[Throwable] {
      override def accept(t: Throwable): Unit = {
        debug("disposablelogflow", t.getMessage)
        val isLogEnabled : Int = configuration.get[Int](s"blockchain.$blockchain.logs")
        debug("islogenable", isLogEnabled.toString)
        if(isLogEnabled>0){
          val timer : Int = configuration.get[Int](s"blockchain.$blockchain.timer")
          debug("disposablelogflow","starting polling logs")
          pollLogs(timer)
        }

      }
    }
    )
  }

   override def pollLogs(timer:Int): Unit ={
    blockDeDepart = web3j.ethBlockNumber().send().getBlockNumber
    val filter = new EthFilter(
      DefaultBlockParameter.valueOf(blockDeDepart),
      DefaultBlockParameterName.LATEST,
      List.empty[String].asJava
    ).addNullTopic()

    web3j.ethGetLogs(filter).sendAsync().thenAccept { logsResponse =>
      logsResponse.getLogs.forEach {
        case log: Log =>
          debug("polllogs", s"Log received: $log")
          notifyListeners(log, Keys.toChecksumAddress(log.getAddress))
        case _ =>
          debug("pollLogs", "Received an unexpected log result type.")
      }

    }.exceptionally { ex =>{

      debug("polllogs",s"Error fetching logs: ${ex.getMessage}")
      null
    }
    }
    after(timer.seconds, summon[ActorSystem].scheduler)(Future.successful(pollLogs(timer)))
  }

  // stop the listening
  def dispose(): Future[Boolean] = {
    entering("dispose()")
    Future(disposableLogFlow.dispose())
      .map(_ => isDisposed)
      .recoverWith {
        case e: Exception =>
          error("dispose", e.getMessage)
          Future.failed[Boolean](e)
      }
  }

  override def isDisposed: Boolean = {
    entering("isDisposed")
    disposableLogFlow.isDisposed
  }
}

object EthereumEventListener {
  def apply(blockchain: String, eventManager: EventManager, webhookListenersDao: WebhookListenersDao, ws: WSClient, configuration: Configuration, executionContext: ExecutionContext, web3j: Web3j): EthereumEventListener = new EthereumEventListener(blockchain, eventManager, webhookListenersDao, ws, configuration, executionContext)(web3j)

  def apply(blockchain: String, eventManager: EventManager, webhookListenersDao: WebhookListenersDao, ws: WSClient, configuration: Configuration, executionContext: ExecutionContext): EthereumEventListener = new EthereumEventListener(blockchain, eventManager, webhookListenersDao, ws, configuration, executionContext)()
}
