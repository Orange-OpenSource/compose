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

package smart_contracts.controllers

import applications.dao.ApplicationDao
import common.controllers.{AuthorizedWithApiKey, ErrorHandler}
import common.utils.TraceUtils
import org.web3j.protocol.Web3j
import org.web3j.protocol.http.HttpService
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import smart_contracts.controllers.blockchain.ethereum.EthereumEventManager
import smart_contracts.dao.{AbiDao, WebhookListenersDao}
import smart_contracts.model.blockchain.DecodedEvent

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class EventController @Inject()(abiDao: AbiDao,
                                machineDBDAO: ApplicationDao,
                                implicit val ec: ExecutionContext,
                                configuration: Configuration,
                                webhooksDao: WebhookListenersDao,
                                cc: ControllerComponents
                               ) extends AbstractController(cc) with TraceUtils with ErrorHandler {

  private val auth = AuthorizedWithApiKey(machineDBDAO, cc, ec)

  private def retrieveEvents(blockchain: String, contractAddress: String, since: String = "0"): Future[Seq[DecodedEvent]] = {
    val web3j: Web3j = Web3j.build(new HttpService(configuration.get[String](s"blockchain.$blockchain.url")))
    val eventManager = EthereumEventManager(abiDao, blockchain, web3j, ec)

    eventManager.getEvents(Map(
      EthereumEventManager.PARAMS_SINCE -> since,
      EthereumEventManager.PARAMS_ADDRESS -> contractAddress
    ))
  }

  def getEvents(blockchain: String, contractAddress: String, since: String = "0"): Action[AnyContent] = auth.authorized.async {

    retrieveEvents(blockchain, contractAddress, since).map((decodedEvents: Seq[DecodedEvent]) => {
      val jsEvents = decodedEvents.map(event => event.value)
      Ok(Json.toJson(jsEvents))
    }).recover {
      case ex: Exception => ex.printStackTrace()
        handleError("getEvent", ex)

    }
  }

  def getEventsByName(blockchain: String, contractAddress: String, eventName: String, since: String = "0"): Action[AnyContent] = auth.authorized.async {
    retrieveEvents(blockchain, contractAddress, since)
      .map(decodedEvents => {
        val jsEvents = decodedEvents.filter(decodedEvent => decodedEvent.event.name == eventName).map(event => event.value)
        Ok(Json.toJson(jsEvents))
      }).recover {
      case ex: Exception => handleError("getEvent", ex)
    }

  }

}
