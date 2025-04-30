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

import common.utils.{Hex, TraceUtils}
import play.api.libs.json.*

import java.math.BigInteger
import java.util
import scala.collection.immutable.ArraySeq
import scala.util.Try
import scala.jdk.CollectionConverters
import scala.jdk.CollectionConverters.ListHasAsScala

case class ReturnValue(index: Int,
                       `type`: String,
                       value: Any) extends TraceUtils{

  def toJson: Try[JsObject] = Try {
    val jsValue: JsValue = `type` match {
      case t: String if t == "bool" => JsBoolean(value.asInstanceOf[Boolean])
      case t: String if t.contains("int") => JsNumber(BigDecimal(value.asInstanceOf[BigInteger]))
      case t: String if t == "address" =>
        debug("toJson",s"'${value.toString}'")
        debug("toJson",s"'${value.getClass.toString}'")
        JsString(value.toString)
      case t: String if t.startsWith("bytes") => JsString("0x" + Hex().toHexString(ArraySeq.unsafeWrapArray(value.asInstanceOf[Array[Byte]])).mkString)
      case t: String if t == "string" => JsString(value.asInstanceOf[String])
      case t: String if t.endsWith("[]") =>
        JsArray(
          value.asInstanceOf[util.ArrayList[Any]]
            .asScala
            .zipWithIndex
            .map(tuple => {
              ReturnValue(tuple._2, t.dropRight(2), tuple._1).toJson.get
            })
          )
      case t => throw new Exception(s"value ($t, ${value.toString}) can not be converted")
    }

    JsObject(
      Seq(
        "index" -> JsNumber(index),
        "type" -> JsString(`type`),
        "value" -> jsValue
      )
    )
  }
}
