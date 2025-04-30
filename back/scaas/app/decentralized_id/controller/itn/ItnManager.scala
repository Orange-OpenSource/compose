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

package decentralized_id.controller.itn

import common.utils.TraceUtils
import decentralized_id.controller.IdentityManager
import decentralized_id.model.{DidDescription, VcDescription}
import play.api.Configuration
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.libs.ws.{EmptyBody, WSClient}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class ItnManager @Inject() (wsClient: WSClient, config:Configuration, network: String ) extends IdentityManager with TraceUtils {

  val version = 1
  def request(action: String) = wsClient.url(s"${config.get[String](s"decentralize-id.$network.url")}/$action")


  override def retrieveVcs():  Future[List[JsObject]] = {
    request("vc-http/get-vcs").get().map { response =>
      if (response.status == 200) {
        (response.json).as[List[JsObject]]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        List.empty[JsObject]
      }
    }
  }

  override def requestToken(): Future[JsObject] = {
    request("vc-http/request-token").get().map { response =>
      if (response.status == 200) {
        (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def retrieveVC(id: String): Future[JsObject] =  {
    request(s"vc-http/get-vc-by-id/$id").get().map { response =>
      if (response.status == 200) {
        (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def createVc(vc: JsObject): Future[JsObject] = {
    request("vc-http/issue-credential").post(vc).map { response =>
      if (response.status == 200) {
        (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def verifyVc(vc: JsObject): Future[JsObject] = {
    request("vc-http/verify-vc").post(vc).map { response =>
      if (response.status == 200) {
        (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def retrieveDids(): Future[JsObject] = {
    request("did").get().map { response =>
      if (response.status == 200) {
          (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def retrieveDid(did: String): Future[JsObject] ={
    request(s"get-did/$did").get().map { response =>
      if (response.status == 200) {
        (response.json).as[JsObject]
      } else {
        println(s"Error: ${response.status} - ${response.body}")
        JsObject.empty
      }
    }
  }

  override def createDid(did: JsObject): Future[JsObject] = {
    request("create-did").post(did).map { response =>
      if (response.status == 200) {
        response.json.as[JsObject]
      } else {
        JsObject.empty
      }
    }
  }
  override def updateDid(did: JsObject): Future[JsObject] ={
    request("update-did").post(did).map { response =>
      if (response.status == 200) {
        response.json.as[JsObject]
      } else {
        JsObject.empty
      }
    }
  }

  override def revokeDid(did: String): Future[Boolean] ={
    request("revoke-did").post(did).map { response =>
      if (response.status == 200) {
        response.json.as[Boolean]
      } else {
        false
      }
    }
  }
}
