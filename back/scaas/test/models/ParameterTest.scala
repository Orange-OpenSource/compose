package models

import org.web3j.abi.datatypes._
import org.web3j.abi.datatypes.generated.{Bytes1, Int256, Uint256}
import play.api.libs.json._
import play.api.test.PlaySpecification
import smart_contracts.model.blockchain.evm.Parameter

import java.math.BigInteger
import java.util
import scala.util.Try

class ParameterTest extends PlaySpecification {

  def typeIsInstantiable(typename: String, jsValue: JsValue): Try[?] = {
    Parameter("test", typename, jsValue)
      .w3Value
      .map(org.web3j.abi.TypeDecoder.instantiateType(typename, _))
  }

  "A parameter W3value" should {
    "return a correct bool" in {
      typeIsInstantiable("bool", JsString("true")).get.asInstanceOf[Bool].getValue mustEqual true
      typeIsInstantiable("bool", JsString("True")).get.asInstanceOf[Bool].getValue mustEqual true
      typeIsInstantiable("bool", JsNumber(1)).get.asInstanceOf[Bool].getValue mustEqual true
      typeIsInstantiable("bool", JsBoolean(true)).get.asInstanceOf[Bool].getValue mustEqual true

      typeIsInstantiable("bool", JsString("false")).get.asInstanceOf[Bool].getValue mustEqual false
      typeIsInstantiable("bool", JsString("False")).get.asInstanceOf[Bool].getValue mustEqual false
      typeIsInstantiable("bool", JsNumber(0)).get.asInstanceOf[Bool].getValue mustEqual false
      typeIsInstantiable("bool", JsBoolean(false)).get.asInstanceOf[Bool].getValue mustEqual false
    }

    "return a correct JSON" in {
      val p = Parameter("test", "bool", JsString("false"))
      Json.toJson(p).toString() mustEqual "{\"name\":\"test\",\"type\":\"bool\",\"value\":\"false\"}"
    }

    "fail on an incorrect bool" in {
      typeIsInstantiable("bool", JsString("not a boolean")).isSuccess mustEqual false
      typeIsInstantiable("bool", JsArray()).isSuccess mustEqual false
    }

    "return a correct int" in {
      typeIsInstantiable("int256", JsNumber(123456)).get.asInstanceOf[Int256].getValue mustEqual new BigInteger("123456")
      typeIsInstantiable("int256", JsString("123456")).get.asInstanceOf[Int256].getValue mustEqual new BigInteger("123456")
    }

    "fail on a incorrect int" in {
      typeIsInstantiable("int8", JsNumber(300)).isSuccess mustEqual false
      typeIsInstantiable("int8", JsNumber(300.5)).isSuccess mustEqual false
      typeIsInstantiable("int8", JsString("300")).isSuccess mustEqual false
      typeIsInstantiable("int8", JsString("300.5")).isSuccess mustEqual false
      typeIsInstantiable("int8", JsString("not a number")).isSuccess mustEqual false
      typeIsInstantiable("int8", JsArray()).isSuccess mustEqual false
      typeIsInstantiable("int8", JsBoolean(true)).isSuccess mustEqual false
    }

    "return a correct uint" in {
      typeIsInstantiable("uint256", JsNumber(123456)).get.asInstanceOf[Uint256].getValue mustEqual new BigInteger("123456")
      typeIsInstantiable("uint256", JsString("123456")).get.asInstanceOf[Uint256].getValue mustEqual new BigInteger("123456")
    }

    "fail on an incorrect uint" in {
      typeIsInstantiable("uint8", JsNumber(-1)).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsNumber(300)).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsNumber(24.5)).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsString("-1")).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsString("300")).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsString("24.5")).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsString("not a number")).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsArray()).isSuccess mustEqual false
      typeIsInstantiable("uint8", JsBoolean(true)).isSuccess mustEqual false
    }

    "return a correct String" in {
      typeIsInstantiable("string", JsString("une phrase")).get.asInstanceOf[Utf8String].getValue mustEqual "une phrase"
      typeIsInstantiable("string", JsNumber(123456)).get.asInstanceOf[Utf8String].getValue mustEqual "123456"
      typeIsInstantiable("string", JsBoolean(true)).get.asInstanceOf[Utf8String].getValue mustEqual "true"
    }

    "fail on an incorrect string" in {
      typeIsInstantiable("string", JsArray()).isSuccess mustEqual false

    }

    "return a correct address" in {
      typeIsInstantiable("address", JsString("0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF")).get.asInstanceOf[Address].getValue mustEqual "0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF".toLowerCase()
      typeIsInstantiable("address", JsNumber(0)).get.asInstanceOf[Address].getValue mustEqual "0x0000000000000000000000000000000000000000"
      typeIsInstantiable("address", JsNumber(BigDecimal("12345678901234567890123456789"))).get.asInstanceOf[Address].getValue mustEqual "0x000000000000000027e41b3246bec9b16e398115"

    }

    "fail on an incorrect address" in {
      typeIsInstantiable("address", JsString("0x not an address")).isSuccess mustEqual false
      typeIsInstantiable("address", JsNumber(-1)).isSuccess mustEqual false
      typeIsInstantiable("address", JsNumber(0.5)).isSuccess mustEqual false
      typeIsInstantiable("address", JsNumber(BigDecimal("12345678901234567890123456789012345678901234567890"))).isSuccess mustEqual false
      typeIsInstantiable("address", JsBoolean(true)).isSuccess mustEqual false
      typeIsInstantiable("address", JsArray()).isSuccess mustEqual false
    }

    "return a correct bytes" in {
      typeIsInstantiable("bytes", JsString("01020304")).get.asInstanceOf[BytesType].getValue mustEqual scala.Array(1, 2, 3, 4)
      typeIsInstantiable("bytes1", JsNumber(-128)).get.asInstanceOf[Bytes1].getValue mustEqual scala.Array(-128)
      typeIsInstantiable("bytes1", JsNumber(0)).get.asInstanceOf[Bytes1].getValue mustEqual scala.Array(0)
      typeIsInstantiable("bytes1", JsNumber(127)).get.asInstanceOf[Bytes1].getValue mustEqual scala.Array(127)
    }

    "fail on an incorrect bytes" in {
      typeIsInstantiable("bytes1", JsString("0102")).isSuccess mustEqual false
      typeIsInstantiable("bytes1", JsNumber(-129)).isSuccess mustEqual false
      typeIsInstantiable("bytes1", JsNumber(128)).isSuccess mustEqual false
      typeIsInstantiable("bytes1", JsBoolean(true)).isSuccess mustEqual false
      typeIsInstantiable("bytes1", JsArray()).isSuccess mustEqual false
    }

    "return a correct array" in {
      val a: util.List[Uint] = typeIsInstantiable("uint[]", JsArray(List(JsString("123"), JsString("456"), JsString("789")))).get.asInstanceOf[org.web3j.abi.datatypes.Array[Uint]].getValue
      a.get(0).getValue mustEqual new BigInteger("123")
      a.get(1).getValue mustEqual new BigInteger("456")
      a.get(2).getValue mustEqual new BigInteger("789")
      val b: util.List[Uint] = typeIsInstantiable("uint[3]", JsArray(List(JsString("123"), JsString("456"), JsString("789")))).get.asInstanceOf[org.web3j.abi.datatypes.Array[Uint]].getValue
      b.get(0).getValue mustEqual new BigInteger("123")
      b.get(1).getValue mustEqual new BigInteger("456")
      b.get(2).getValue mustEqual new BigInteger("789")
      val c: util.List[Uint] = typeIsInstantiable("uint[3]", JsArray(List(JsNumber(123), JsNumber(456), JsNumber(789)))).get.asInstanceOf[org.web3j.abi.datatypes.Array[Uint]].getValue
      c.get(0).getValue mustEqual new BigInteger("123")
      c.get(1).getValue mustEqual new BigInteger("456")
      c.get(2).getValue mustEqual new BigInteger("789")
    }

    "fail on an incorrect  array" in {
      typeIsInstantiable("uint[2]", JsArray(List(JsString("123"), JsString("456"), JsString("789")))).isSuccess mustEqual false
      typeIsInstantiable("uint[2]", JsArray(List(JsBoolean(true), JsBoolean(true)))).isSuccess mustEqual false
      typeIsInstantiable("uint[2]", JsString("123")).isSuccess mustEqual false
      typeIsInstantiable("uint[2]", JsNumber(123)).isSuccess mustEqual false
      typeIsInstantiable("uint[2]", JsBoolean(true)).isSuccess mustEqual false
    }

    "return a correct array of array" in {
      val a = typeIsInstantiable("bool[][]",
        JsArray(List(
          JsArray(List(JsString("true"), JsString("True"))),
          JsArray(List(JsString("false"), JsString("False"))),
        ))
      ).get.asInstanceOf[org.web3j.abi.datatypes.Array[org.web3j.abi.datatypes.Array[Bool]]].getValue

      a.get(0).getValue.get(0).getValue mustEqual true
      a.get(0).getValue.get(1).getValue mustEqual true
      a.get(1).getValue.get(0).getValue mustEqual false
      a.get(1).getValue.get(1).getValue mustEqual false
    }

  }

}
