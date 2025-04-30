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

package smart_contracts.modules

import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import play.api.ConfigLoader.seqStringLoader
import play.api.Configuration
import play.api.inject.ApplicationLifecycle
import play.api.libs.ws.WSClient
import smart_contracts.controllers.blockchain.EventManager
import smart_contracts.controllers.blockchain.ethereum.{EthereumEventListener, EthereumEventManager}
import smart_contracts.dao.{AbiDao, WebhookListenersDao}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EventService @Inject()(lifecycle: ApplicationLifecycle,
                             abiDao: AbiDao,
                             webhookListenersDao: WebhookListenersDao,
                             wsClient: WSClient,
                             configuration: Configuration,
                             implicit val executionContext: ExecutionContext) {

  val listeners: Seq[EthereumEventListener] = configuration.get("blockchain.active")
    .map(blockchain => {
      val web3j = Web3j.build(new HttpService(configuration.get[String](s"blockchain.$blockchain.url")))
      val eventManager: EventManager = EthereumEventManager(abiDao, blockchain, web3j, executionContext)
      EthereumEventListener(blockchain, eventManager, webhookListenersDao, wsClient, configuration, executionContext)
    })

  // Shut-down hook
  lifecycle.addStopHook { () =>
    Future.sequence(listeners.map(listener => listener.dispose()))

  }
}
