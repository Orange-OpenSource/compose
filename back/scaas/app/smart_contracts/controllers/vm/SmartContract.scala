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

package smart_contracts.controllers.vm

import org.web3j.crypto.Credentials
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.model.blockchain.evm.Parameter
import smart_contracts.model.blockchain.{Event, Function}

import scala.util.Try

/**
 * Base trait of a smart contract, blockchain independant.
 */
trait SmartContract {

  /**
   * The name of the smart contract
   */
  val name: String

  /**
   * The address at which the smart contract is deployed
   */
  val address: String

  /**
   * The set of events that this smart contract can generate
   */
  val events: Set[Event]

  /**
   * The set of functions that this smart contract can use
   */
  val functions: Set[Function]

  /**
   * find a function by its name and input parameter type
   *
   * @param name       the name of the desired function
   * @param parameters the sequence of parameters of the desired function
   * @return `Some(function)` if it exists, `None` if it does not.
   */
  def findFunction(name: String, parameters: Seq[Parameter]): Option[Function] =
    functions.find(f => f.name == name && f.inputParameterType == parameters.map(_.`type`))

  /**
   * invoke a function with given parameters on the blockchain
   *
   * @param function   the function to be invoked
   * @param parameters the parameter values given to the invoked function
   * @return the receipt of the tx
   */
  def prepareAndInvoke(function: Function, tokenValue: BigInt,parameters: Seq[Parameter])(blockchainManager: BlockchainManager, credentials: Credentials): Try[Any]

  def prepareAndInvokeView(function: Function, parameters: Seq[Parameter], senderAddress: String)
                          (blockchainManager: BlockchainManager): Try[Any]
}
