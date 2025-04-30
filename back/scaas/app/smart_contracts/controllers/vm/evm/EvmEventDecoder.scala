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

package smart_contracts.controllers.vm.evm

import common.utils.TraceUtils
import org.web3j.protocol.core.methods.response.Log
import smart_contracts.controllers.vm.{EventDecoder, SmartContract}
import smart_contracts.model.blockchain.DecodedEvent

import scala.util.Try

class EvmEventDecoder(smartContract: SmartContract) extends EventDecoder(smartContract) with TraceUtils {

  override def decode(log: Any): Try[DecodedEvent] = Try {
    entering("decode(Any)")
    val evmLog = log.asInstanceOf[Log]
    val topic = evmLog.getTopics.get(0)
    smartContract
      .events
      .find(_.encoded == topic)
      .map(event => DecodedEvent(event, event.decode(log)))
      .get
  }

}

object EvmEventDecoder extends TraceUtils {

  def apply(smartContract: SmartContract): EvmEventDecoder = new EvmEventDecoder(smartContract)

}
