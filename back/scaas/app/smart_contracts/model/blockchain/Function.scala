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

package smart_contracts.model.blockchain

import play.api.libs.json.JsValue

trait Function {

  val name: String

  val isConstant: Boolean

  val inputParameterType: Seq[String]

  val outputParameterType: Seq[String]

  val functionInfo: JsValue

}
