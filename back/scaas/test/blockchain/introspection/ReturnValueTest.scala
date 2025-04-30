package blockchain.introspection

import org.web3j.abi.datatypes.Address
import play.api.test.PlaySpecification
import smart_contracts.model.blockchain.ReturnValue

import java.util

class ReturnValueTest extends PlaySpecification {

  "ReturnValue" should {

    "throw an exception if a return value type is not taken into account" in {
      ReturnValue(0,"type42","toto0x34").toJson.isFailure mustEqual true
    }

    "parse an array of addresses" in {

      val addr1 : Address = new Address("0xe77c7063324ecee7c27004b1eb4d028dc10d726f")
      val addr2 : Address = new Address("0x2b17f5a2c1149c43870fcd612e828a2c537dc76f")
      val addr3 : Address = new Address("0xbca1e4c4cda48aba284146a6f34b0fe8afa2ca97")

      val value : util.ArrayList[Address] = new util.ArrayList[Address]()

      value.add(addr1)
      value.add(addr2)
      value.add(addr3)

      val tag = "address[]"

      ReturnValue(0, tag, value).toJson.toString mustEqual "Success({\"index\":0,\"type\":\"address[]\",\"value\":[{\"index\":0,\"type\":\"address\",\"value\":\"0xe77c7063324ecee7c27004b1eb4d028dc10d726f\"},{\"index\":1,\"type\":\"address\",\"value\":\"0x2b17f5a2c1149c43870fcd612e828a2c537dc76f\"},{\"index\":2,\"type\":\"address\",\"value\":\"0xbca1e4c4cda48aba284146a6f34b0fe8afa2ca97\"}]})"


    }

  }

}
