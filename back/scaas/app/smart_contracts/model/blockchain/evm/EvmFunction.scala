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

package smart_contracts.model.blockchain.evm

import common.utils.TraceUtils
import org.web3j.protocol.core.methods.response.AbiDefinition
import play.api.libs.json.{JsValue, Json}
import smart_contracts.model.blockchain.Function

import scala.jdk.CollectionConverters.IterableHasAsScala

case class EvmFunction(abiDef: AbiDefinition) extends Function with TraceUtils {

  private lazy val inputs: List[AbiDefinition.NamedType] = abiDef.getInputs.asScala.toList
  private lazy val outputs: List[AbiDefinition.NamedType] = abiDef.getOutputs.asScala.toList

  val name: String = abiDef.getName

  val isConstant: Boolean = Set("pure", "view").contains(abiDef.getStateMutability)

   val functionInfo: JsValue = Json.obj(
    "name" -> name,
    "constant" -> isConstant.toString,
    "parameters" -> namedParametersToJson(inputs),
    "output" -> outputs.map { p =>
      p.getType
    }
  )

  val inputParameterType: Seq[String] = inputs.map(_.getType)

  val outputParameterType: Seq[String] = outputs.map(_.getType)

  override def toString: String = {
    functionInfo.toString()
  }

}
