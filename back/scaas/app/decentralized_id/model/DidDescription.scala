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

package decentralized_id.model

import play.api.libs.json._


case class DidDescription(urn: String, services: Seq[String])

object DidDescription {
  implicit val format: OFormat[DidDescription] = Json.format[DidDescription]
}
