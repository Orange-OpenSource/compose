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

package smart_contracts.model

import play.api.libs.json._

sealed trait Value {
  def rawValue: Any
}

case class StringValue(value: String) extends Value {
  def rawValue: Any = value
}

case class IntValue(value: BigInt) extends Value {
  def rawValue: Any = value
}

case class BoolValue(value: Boolean) extends Value {
  def rawValue: Any = value
}

object Value {
  implicit val format: Format[Value] = new Format[Value] {
    def reads(json: JsValue): JsResult[Value] = {
      json.validate[String].map(smart_contracts.model.StringValue.apply) orElse
      json.validate[BigInt].map(smart_contracts.model.IntValue.apply) orElse
      json.validate[Boolean].map(smart_contracts.model.BoolValue.apply)
    }

    def writes(value: Value): JsValue = value match {
      case StringValue(v) => JsString(v)
      case IntValue(v) => JsNumber(BigDecimal(v))
      case BoolValue(v) => JsBoolean(v)
    }
  }
}

case class ContractParameters(
                               name: String,
                               valueType: String,
                               value: Value
                             )

object ContractParameters {
  implicit val format: OFormat[ContractParameters] = Json.format[ContractParameters]
}
