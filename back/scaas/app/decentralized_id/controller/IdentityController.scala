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

package decentralized_id.controller

import applications.dao.ApplicationDao
import common.controllers.*
import companies.controllers.CompaniesManager
import decentralized_id.controller.itn.ItnManager
import decentralized_id.factory.IdentityFactory
import login.dao.TokensDao
import play.api.libs.json.*
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, *}
import users.dao.UserDao
import play.api.Configuration

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class IdentityController @Inject()(identityFactory: IdentityFactory,
                                   tokensDao: TokensDao,
                                   ws: WSClient,
                                   applicationDao: ApplicationDao,
                                   config:Configuration,
                                   implicit val ec: ExecutionContext,
                                   cc: ControllerComponents) extends AbstractController(cc) with ErrorHandler {

  private val auth = AuthorizedWithApiKey(applicationDao, cc, ec)

  def getDids(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("getDids")
    identityFactory.build(ws ,config, network)
    .retrieveDids().map(dids => {
        Ok(Json.toJson(dids))
      })
      .recover {
        case exception: Exception =>
          handleError("getDids", exception)
      }
  }

  def getDid(network: String,did : String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("getDid")
    identityFactory.build(ws, config, network)
      .retrieveDid(did).map(did => {
        Ok(Json.toJson(did))
      })
      .recover {
        case exception: Exception =>
          handleError("getDid", exception)
      }
  }

  def createDid(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("createDid")
    val did:JsObject =  req.body.asJson.get.as[JsObject]
    identityFactory.build(ws, config, network)
      .createDid(did).map(did => {
        Created(Json.toJson(did))
      })
      .recover {
        case exception: Exception =>
          handleError("createDid", exception)
      }
  }

  def updateDid(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("updateDid")
    val did: JsObject = req.body.asJson.get.as[JsObject]
    identityFactory.build(ws, config, network)
      .updateDid(did).map(did => {
        Created(Json.toJson(did))
      })
      .recover {
        case exception: Exception =>
          handleError("updateDid", exception)
      }
  }

  def revokeDid(network: String,did : String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("revokeDid")

    identityFactory.build(ws, config, network)
      .revokeDid(did).map(did => {
        Ok("did revoked")
      })
      .recover {
        case exception: Exception =>
          handleError("revokeDid", exception)
      }
  }

  def requestToken(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("requesttoken")

    identityFactory.build(ws, config, network)
      .requestToken().map(did => {
        Ok(Json.toJson(did))
      })
      .recover {
        case exception: Exception =>
          handleError("requesttoken", exception)
      }
  }

  def createVC(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("createVC")
    val vc: JsObject = req.body.asJson.get.as[JsObject]
    identityFactory.build(ws, config, network)
      .createVc(vc).map(vc => {
        Created(Json.toJson(vc))
      })
      .recover {
        case exception: Exception =>
          handleError("createVC", exception)
      }
  }

  def verifyVC(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("createVC")
    val vc: JsObject = req.body.asJson.get.as[JsObject]
    identityFactory.build(ws, config, network)
      .verifyVc(vc).map(bool => {
        Ok(Json.toJson(bool))
      })
      .recover {
        case exception: Exception =>
          handleError("createVC", exception)
      }
  }

  def getVcs(network: String): Action[AnyContent] = auth.authorized(parse.anyContent).async { req =>
    entering("getVcs")
    identityFactory.build(ws, config, network)
      .retrieveVcs().map(vcs => {
        Ok(Json.toJson(vcs))
      })
      .recover {
        case exception: Exception =>
          handleError("getVcs", exception)
      }
  }

}
