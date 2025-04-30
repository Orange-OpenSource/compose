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

package smart_contracts.model.dao

import play.api.libs.json._
import reactivemongo.api.bson.{BSONDocumentHandler, Macros}

case class ContractOptions(
                            network: String,
                            version: String,
                            evmVersion: Option[String],
                            optimizerRuns: Option[Int],
                            httpProxy: Option[String] = None,
                            httpsProxy: Option[String] = None
                          )

object ContractOptions {
  implicit val handler: BSONDocumentHandler[ContractOptions] = Macros.handler[ContractOptions]
  implicit val format: OFormat[ContractOptions] = Json.format[ContractOptions]
}

