package models

import play.api.libs.json._
import play.api.test.PlaySpecification
import smart_contracts.model.{BoolValue, ContractParameters, IntValue, StringValue}

class ContractParametersTest extends PlaySpecification {

  "ContractParameters value extraction" should {

    "return the correct raw value for an address StringValue" in {
      val address = StringValue("0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF")
      address.rawValue mustEqual "0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF"
    }

    "return the correct raw value for a standard StringValue" in {
      val string = StringValue("test")
      string.rawValue mustEqual "test"
    }

    "return the correct raw value for an empty StringValue" in {
      val string = StringValue("")
      string.rawValue mustEqual ""
    }

    "return the correct raw value for a StringValue representing an integer" in {
      val string = StringValue("123")
      string.rawValue mustEqual "123"
    }

    "return the correct raw value for a valid IntValue" in {
      val integer = IntValue(BigInt(123))
      integer.rawValue mustEqual BigInt(123)
    }

    "return the correct raw value for an IntValue initialized to zero" in {
      val integer = IntValue(BigInt(0))
      integer.rawValue mustEqual BigInt(0)
    }

    "return the correct raw value for a large IntValue" in {
      val integer = IntValue(BigInt("123456789012345678901234567890"))
      integer.rawValue mustEqual BigInt("123456789012345678901234567890")
    }

    "return the correct raw value for a BoolValue set to true" in {
      val boolean = BoolValue(true)
      boolean.rawValue mustEqual true
    }

    "return the correct raw value for a BoolValue set to false" in {
      val boolean = BoolValue(false)
      boolean.rawValue mustEqual false
    }

  }

  "ContractParameters JSON serialization" should {

    "correctly serialize and deserialize ContractParameters with an address StringValue" in {
      val params = ContractParameters("param0", "address", StringValue("0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF"))
      val json = Json.toJson(params)
      (json \ "name").as[String] mustEqual "param0"
      (json \ "valueType").as[String] mustEqual "address"
      (json \ "value").as[String] mustEqual "0xdCad3a6d3569DF655070DEd06cb7A1b2Ccd1D3AF"
      json.validate[ContractParameters].get mustEqual params
    }

    "correctly serialize and deserialize ContractParameters with a standard StringValue" in {
      val params = ContractParameters("param1", "string", StringValue("test"))
      val json = Json.toJson(params)
      (json \ "name").as[String] mustEqual "param1"
      (json \ "valueType").as[String] mustEqual "string"
      (json \ "value").as[String] mustEqual "test"
      json.validate[ContractParameters].get mustEqual params
    }

    "correctly serialize and deserialize ContractParameters with an IntValue" in {
      val params = ContractParameters("param2", "int", IntValue(BigInt(123)))
      val json = Json.toJson(params)
      (json \ "name").as[String] mustEqual "param2"
      (json \ "valueType").as[String] mustEqual "int"
      (json \ "value").as[BigInt] mustEqual BigInt(123)
      json.validate[ContractParameters].get mustEqual params
    }

    "correctly serialize and deserialize ContractParameters with a BoolValue set to true" in {
      val params = ContractParameters("param3", "bool", BoolValue(true))
      val json = Json.toJson(params)
      (json \ "name").as[String] mustEqual "param3"
      (json \ "valueType").as[String] mustEqual "bool"
      (json \ "value").as[Boolean] mustEqual true
      json.validate[ContractParameters].get mustEqual params
    }

    "correctly handle null values" in {
      val json = Json.obj(
        "name" -> "param4",
        "valueType" -> "string",
        "value" -> JsNull
      )
      json.validate[ContractParameters].isError mustEqual true
    }

  }

}
