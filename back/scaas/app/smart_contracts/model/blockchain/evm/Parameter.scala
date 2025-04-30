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

import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.{Array => w3Array, _}
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json._

import java.math.BigInteger
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.util.Try

case class Parameter(name: String,
                     `type`: String,
                     value: JsValue
                    ) {

  def w3Value: Try[Object] = Try {

    // reference of type in solidity : https://solidity.readthedocs.io/en/v0.6.3/types.html
    // types:
    // bool --> ok
    // int / uint / 8 -> 256 --> ok
    // fixed / ufixed / fixedMxN --> not yet managed by solidity...
    // address --> ok
    // contract --> no, address instead
    // fixed size bytes 1->32 --> ok
    // dynamic byte array (bytes) --> ok
    // string --> ok
    // enum --> ok
    // struct --> experimental in abiV2, maybe later
    // arrays -- ... ok-ish, seems ok for web3j, but fails :/
    // mappings -->  Mapping types can only have a data location of "storage" and thus only be parameters or return variables for internal or library functions


    // see
    // org.web3j.abi.TypeDecoder.instantiateType()
    // what is returned must be compatible with this function, which is managed by two main functions:
    //   * instantiateAtomicType:
    //      - NumericType => uint, intX, uintX => BigInteger / BigDecimal / String / Array[Byte] / Double / Float / Number
    //      - BytesType => bytes, bytesX => String (hexadecimal) / BigInteger / Array[Byte]
    //      - Utf8String => string => String
    //      - Address => address => String / BigInteger
    //      - Bool => bool (, boolean) => Boolean
    //   * instantiateArrayType:
    //      - Arrays (static, dynamic) =>T[], T[n] => java.util.List[T]
    // special case :
    // "enum" is managed as an integer in abi encoding ==> Numeric/ BigInteger
    // "byte" is managed as a bytes1 in abi encoding ==> BytesTypes/String


    def toValue(t: String, jsValue: JsValue): Any = {
      val typeReference = TypeReference.makeTypeReference(t)
      val referenceClass = typeReference.getClassType

      jsValue match {
        case boolean: JsBoolean => boolean.as[Boolean]
        case JsString(value) if classOf[Bool].isAssignableFrom(referenceClass) => value.toBoolean
        case JsString(value) if classOf[Int].isAssignableFrom(referenceClass) => new BigInteger(value)
        case JsString(value) if classOf[Uint].isAssignableFrom(referenceClass) => new BigInteger(value)
        case JsNumber(value) => new BigInteger(value.toString)
        case JsString(value) => value
        case JsArray(value) if classOf[w3Array[?]].isAssignableFrom(referenceClass) =>
          val subType = t.reverse.dropWhile(_ != '[').tail.reverse
          value.toList.map(js => toValue(subType, js)).asJava
        case _ => throw new Exception(s"Value ($t, ${jsValue.toString}) is not correctly formatted")
      }
    }

    toValue(`type`, value).asInstanceOf[Object]
  }

}

object Parameter {
  implicit val format: OFormat[Parameter] = Json.format[Parameter]
}
