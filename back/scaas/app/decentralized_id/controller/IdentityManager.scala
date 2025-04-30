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

import play.api.libs.json.JsObject

import scala.concurrent.Future

trait IdentityManager {


  def requestToken(): Future[JsObject]

  def retrieveVcs(): Future[List[JsObject]]

  def retrieveVC(id: String): Future[JsObject]

  def createVc(vc: JsObject): Future[JsObject]

  def verifyVc(vc: JsObject): Future[JsObject]

  def retrieveDids(): Future[JsObject]

  def retrieveDid(did: String): Future[JsObject]

  def createDid(did: JsObject): Future[JsObject]

  def updateDid(did: JsObject): Future[JsObject]

  def revokeDid(did: String): Future[Boolean]
}
