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

package decentralized_id.factory

import common.exceptions.ParseException
import common.utils.TraceUtils
import decentralized_id.controller.IdentityManager
import decentralized_id.controller.itn.ItnManager
import play.api.Configuration
import play.api.libs.ws.WSClient

class IdentityFactory extends TraceUtils{
  def build(ws: WSClient, configuration: Configuration, network: String): IdentityManager = {

    configuration.get[String](s"decentralize-id.$network.type") match {
      case "itn" => ItnManager(ws,configuration,network)

      case _ => throw ParseException("network type does not exist")
    }

  }
}
