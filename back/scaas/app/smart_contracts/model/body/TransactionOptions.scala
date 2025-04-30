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

package smart_contracts.model.body

import play.api.libs.json._


case class TransactionOptions(gasLimit: BigInt,
                              gasPremium: BigInt,
                              feeCap: BigInt)


object TransactionOptions {
  implicit val BigIntWrite: Writes[BigInt] = (bigInt: BigInt) => JsString(bigInt.toString())

  implicit val BigIntRead: Reads[BigInt] = Reads {
    case JsString(value) => JsSuccess(scala.math.BigInt(value))
    case JsNumber(value) => JsSuccess(value.toBigInt)
    case _ => JsError(s"Invalid BigInt")
  }

  implicit val format: OFormat[TransactionOptions] = Json.format[TransactionOptions]

}
