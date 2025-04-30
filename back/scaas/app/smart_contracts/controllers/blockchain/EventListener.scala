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

import common.utils.TraceUtils
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.WSClient
import reactivemongo.api.bson.{BSONDocument, BSONString}
import smart_contracts.controllers.vm.evm.EvmEventDecoder
import smart_contracts.dao.WebhookListenersDao

import scala.concurrent.{ExecutionContext, Future}

abstract class EventListener(blockchain: String, eventManager: EventManager, webhookListenersDao: WebhookListenersDao, ws: WSClient, implicit val executionContext: ExecutionContext) extends TraceUtils {

  def notifyListeners(log: Any, address: String): Future[Seq[Unit]] = {
    entering("notifyListeners")
    

    eventManager.decode(log, address)
      .flatMap(decodedEvent => {
        
        webhookListenersDao.find(
          BSONDocument(
            "blockchain" -> BSONString(blockchain),
            "contractAddress" -> BSONString(address),
            "eventName" -> BSONString(decodedEvent.event.name)
          ), 100).flatMap(listeners => {
          Future.sequence(
            listeners
              .map { listeningRequest =>
                debug("onEvent", s"sending event [${decodedEvent.value}] to ${listeningRequest.url}")
                ws.url(listeningRequest.url)
                  .addHttpHeaders("Content-Type" -> "application/json")
                  .post(decodedEvent.value)
                  .map { response =>
                    debug("notifyListeners", s"response arrived ${response.status}")
                    if (response.status == 200)
                      debug("onEvent", s"${listeningRequest.url} response status: ${response.status} - ${response.statusText}")
                    else
                      debug("onEvent", s"${listeningRequest.url} response status: ${response.status} - ${response.statusText} body:\n${response.body}")
                  }.recover {
                    case e: Exception => info("onEvent", s"Error : $e")
                  }
              })
        })
      })
  }

  def dispose(): Future[Boolean]
  def pollLogs(timer:Int):Unit
  def isDisposed: Boolean

}
