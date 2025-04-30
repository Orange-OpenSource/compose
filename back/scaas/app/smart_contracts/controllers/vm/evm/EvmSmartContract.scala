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

import com.fasterxml.jackson.databind.ObjectMapper
import common.exceptions.BlockchainException
import common.utils.TraceUtils
import org.web3j.abi.{FunctionEncoder, FunctionReturnDecoder, datatypes}
import org.web3j.crypto.Credentials
import org.web3j.protocol.ObjectMapperFactory
import org.web3j.protocol.core.methods.response.AbiDefinition
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.controllers.vm.SmartContract
import smart_contracts.model._
import smart_contracts.model.blockchain.evm.{EvmEvent, EvmFunction, Parameter}
import smart_contracts.model.blockchain.{Event, ReturnValue}
import smart_contracts.model.dao.Abi

import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.jdk.CollectionConverters.ListHasAsScala

import scala.util.{Success, Try}

class EvmSmartContract(abi: Abi) extends SmartContract with TraceUtils {

  private val abiDefinition: Set[AbiDefinition] = {
    val objectMapper: ObjectMapper = ObjectMapperFactory.getObjectMapper
    objectMapper.readValue(abi.abi, classOf[Array[AbiDefinition]]).toSet
  }

  override val functions: Set[blockchain.Function] = abiDefinition.filter(_.getType == "function").map(smart_contracts.model.blockchain.evm.EvmFunction.apply)

  def functionBuilder(parameters: Seq[Parameter], function: blockchain.Function): Try[datatypes.Function] = Try {
    entering("functionBuilder")
    val paramTriedValues = parameters.map(_.w3Value)


    val paramValues = paramTriedValues.map(_.get)

    debug("invoke",
      s"invoking function [${function.name} : " +
        s"${function.inputParameterType.mkString("(", ",", ")")} => " +
        s"${function.outputParameterType.mkString("(", ",", ")")}] " +
        s"of contract [${abi.name}], on blockchain [ethereum], " +
        s"at address [${abi.address}], " +
        s"with values [${paramValues.mkString("(", ",", ")")}]")

    org.web3j.abi.FunctionEncoder.makeFunction(
      function.name,
      function.inputParameterType.asJava,
      paramValues.asJava,
      function.outputParameterType.asJava
    )
  }

  def prepareAndInvokeView(function: blockchain.Function, parameters: Seq[Parameter], senderAddress: String)(blockchainManager: BlockchainManager): Try[List[ReturnValue]] = Try {
    entering("prepareAndInvokeView")
    val triedResponse: Try[List[ReturnValue]] = for {
      f <- functionBuilder(parameters, function)
      encodedFunction <- Success(FunctionEncoder.encode(f))
      response <- blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)
        .map { rawResult =>
          FunctionReturnDecoder
            .decode(rawResult, f.getOutputParameters)
            .asScala
            .toList
            .zipWithIndex
            .map(tuple => {
              ReturnValue(tuple._2, tuple._1.getTypeAsString, tuple._1.getValue)
            })
        }
    } yield response

    triedResponse.get
  }

  private def prepare(function: blockchain.Function, parameters: Seq[Parameter]): Try[String] = Try {
    entering("prepare")
    val fTried = functionBuilder(parameters, function)
    if (fTried.isFailure)
      BlockchainException(s"Can't build the function : ${function.toString}")
    val f = fTried.get
    FunctionEncoder.encode(f)
  }

  def invoke(blockchainManager: BlockchainManager, tokenValue: BigInt,encodedFunction: String)(credentials: Credentials): Try[String] = {
    entering("invoke")
    blockchainManager.invoke(encodedFunction, tokenValue, abi.address)(credentials)
  }

  def prepareAndInvoke(function: blockchain.Function, tokenValue: BigInt , parameters: Seq[Parameter])
                      (blockchainManager: BlockchainManager, credentials: Credentials): Try[String] = Try {
    entering("prepareAndInvoke")
    val encodedFunctionTried = prepare(function, parameters)
    invoke(blockchainManager, tokenValue, encodedFunctionTried.get)(credentials).get
  }

  /**
   * The name of the smart contract
   */
  override val name: String = abi.name
  /**
   * The address at which the smart contract is deployed
   */
  override val address: String = abi.address
  /**
   * The set of events that this smart contract can generate
   */
  override val events: Set[Event] = abiDefinition.filter(_.getType == "event").map(smart_contracts.model.blockchain.evm.EvmEvent.apply)
}

object EvmSmartContract {
  def apply(abi: Abi): EvmSmartContract = new EvmSmartContract(abi)
}


