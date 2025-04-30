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

package smart_contracts.model.body

import play.api.libs.json.{JsObject, Json, OFormat}

case class Deployment(projectId: String,
                      name: String,
                      blockchain: String,
                      address: String,
                      abi: JsObject,
                      userId: String)

object Deployment {
  implicit val format: OFormat[Deployment] = Json.format[Deployment]
}
