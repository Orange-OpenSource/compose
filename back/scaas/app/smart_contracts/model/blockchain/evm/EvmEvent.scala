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

import org.web3j.abi.datatypes.{Event => W3Event}
import org.web3j.abi.{EventEncoder, FunctionReturnDecoder, TypeReference}
import org.web3j.protocol.core.methods.response.{AbiDefinition, Log}
import play.api.libs.json.{JsObject, JsValue, Json}
import smart_contracts.model.blockchain.Event

import java.util
import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.ListHasAsScala
import scala.jdk.CollectionConverters.SeqHasAsJava

case class EvmEvent(abiDef: AbiDefinition) extends Event {

  private lazy val parameters: List[AbiDefinition.NamedType] = abiDef.getInputs.asScala.toList

  // https://stackoverflow.com/questions/33594587/interop-scala-with-java-getting-but-java-defined-trait-list-is-invariant-in-type
  private lazy val typeReferenceList: util.List[TypeReference[?]] = (parameters.map { nt =>
    org.web3j.abi.TypeReference.makeTypeReference(nt.getType, nt.isIndexed, false)
  }: List[TypeReference[?]]).asJava

  private lazy val w3event = new W3Event(name, typeReferenceList)

  val name: String = abiDef.getName

  override val eventInfo: JsObject = Json.obj(
    "name" -> name,
    "parameters" -> namedParametersToJson(parameters)
  )

  override val encoded: String = EventEncoder.encode(w3event)

  override val decode: Any => JsValue = { log =>
    val l = log.asInstanceOf[Log]

    val nonIndexedValues = FunctionReturnDecoder
      .decode(l.getData, w3event.getNonIndexedParameters).asScala

      .map(param => param.getTypeAsString match {
        case p: "bytes32" => param.getValue.asInstanceOf[Array[Byte]].map("%02x".format(_)).mkString
        case p: Any => param.getValue.toString
      })


    val indexedValues = (l.getTopics.asScala.drop(1) zip w3event.getIndexedParameters.asScala).map {
      case (s, t) =>
        t.getType.getTypeName match {
          case p: "org.web3j.abi.datatypes.generated.Bytes32" => FunctionReturnDecoder.decodeIndexedValue(s, t).getValue.asInstanceOf[Array[Byte]].map("%02x".format(_)).mkString
          case p: Any => FunctionReturnDecoder.decodeIndexedValue(s, t).getValue.toString
        }

    }


    val (indexedParams, nonIndexedParams) = parameters.partition(_.isIndexed)

    val params = (indexedParams.zip(indexedValues) ++ nonIndexedParams.zip(nonIndexedValues))
      .toMap
      .map { case (p, v) =>
        Json.obj(
          "name" -> p.getName,
          "type" -> p.getType,
          "value" -> v
        )
      }

    Json.obj(
      "address" -> l.getAddress,
      "blockNumber" -> l.getBlockNumber,
      "name" -> name,
      "parameters" -> params
    )
  }
}
