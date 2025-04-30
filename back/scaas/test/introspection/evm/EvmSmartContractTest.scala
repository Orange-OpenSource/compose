package introspection.evm

import common.exceptions.BlockchainException
import org.mockito.Mockito.{mock, when}
import org.specs2.mutable.Specification
import org.web3j.abi.FunctionEncoder
import org.web3j.crypto.Credentials
import play.api.Configuration
import play.api.libs.json.*
import smart_contracts.controllers.blockchain.ethereum.EthereumManager
import smart_contracts.controllers.vm.evm.EvmSmartContract
import smart_contracts.model.blockchain.evm.Parameter
import smart_contracts.model.dao.Abi

import scala.util.Try

class EvmSmartContractTest extends Specification {
  sequential
  val configuration: Configuration = mock(classOf[Configuration])
  val credentials: Credentials = mock(classOf[Credentials])

  val abi: Abi = Abi(Some("_id1"), "my_abi", "abf-testnet",
    """
    [
      {
        "inputs": [
          {
            "internalType": "bytes",
            "name": "n",
            "type": "bytes"
          },
          {
            "internalType": "bytes",
            "name": "e",
            "type": "bytes"
          }
        ],
        "name": "addKey",
        "outputs": [],
        "stateMutability": "nonpayable",
        "type": "function"
      },
      {
        "inputs": [
          {
            "internalType": "Storage.UFixed256x18",
            "name": "a",
            "type": "uint256"
          }
        ],
        "name": "floor",
        "outputs": [
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "stateMutability": "pure",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "retrieve",
        "outputs": [
          {
            "internalType": "bytes",
            "name": "",
            "type": "bytes"
          },
          {
            "internalType": "uint256",
            "name": "",
            "type": "uint256"
          }
        ],
        "stateMutability": "view",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "retrieveString",
        "outputs": [
          {
            "internalType": "string",
            "name": "",
            "type": "string"
          },
          {
            "internalType": "int256",
            "name": "",
            "type": "int256"
          }
        ],
        "stateMutability": "view",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "retrieveInt",
        "outputs": [
          {
            "internalType": "int256",
            "name": "",
            "type": "int256"
          }
        ],
        "stateMutability": "view",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "retrieveUInt8",
        "outputs": [
          {
            "internalType": "uint8",
            "name": "",
            "type": "uint8"
          }
        ],
        "stateMutability": "view",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "retrieveAddresses",
        "outputs": [
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          },
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          },
          {
            "internalType": "address",
            "name": "",
            "type": "address"
          }
        ],
        "stateMutability": "pure",
        "type": "function"
      },
      {
        "inputs": [],
        "name": "getChoice",
        "outputs": [
          {
            "internalType": "enum Storage.ActionChoices",
            "name": "",
            "type": "uint8"
          }
        ],
        "stateMutability": "view",
        "type": "function"
      },
      {
            "inputs": [],
            "name": "wrongType",
            "outputs": [
              {
                "internalType": "starship",
                "name": "",
                "type": "troopers"
              }
            ],
            "stateMutability": "view",
            "type": "function"
          },
      {
        "inputs": [
          {
            "internalType": "bytes",
            "name": "val",
            "type": "bytes"
          }
        ],
        "name": "get_bit_length",
        "outputs": [
          {
            "internalType": "uint256",
            "name": "res",
            "type": "uint256"
          }
        ],
        "stateMutability": "pure",
        "type": "function"
      }
    ]
    """,
    "0x576B0794736878637b829B2EDFAD7C1223CF6F1A",
    "userId"
  )

  "AbiIntrospectionSmartContractTest" should {

    "prepare and invoke a view/pure function with an enum type as return return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("getChoice", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)
      println(encodedFunction)
      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x0000000000000000000000000000000000000000000000000000000000000002"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"uint8\",\"value\":2}]"
    }

    "prepare and invoke a view/pure function with a custom type as return return value" in {
      val ps = List(Parameter("a", "uint256", JsNumber(BigDecimal("42000000000000000000"))))

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("floor", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x000000000000000000000000000000000000000000000000000000000000002a"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"uint256\",\"value\":42}]"
    }

    "prepare and invoke a view/pure function with a address,address,address return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("retrieveAddresses", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x0000000000000000000000008c1f579415601dbf5817e8b7cbcd43f098a3b4fb000000000000000000000000412720c9be475aebe101abd290a1ef2418f3bdaf000000000000000000000000290526a710c2bd6abb22d2f2bdeae518938c4a2b"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"address\",\"value\":\"0x8c1f579415601dbf5817e8b7cbcd43f098a3b4fb\"},{\"index\":1,\"type\":\"address\",\"value\":\"0x412720c9be475aebe101abd290a1ef2418f3bdaf\"},{\"index\":2,\"type\":\"address\",\"value\":\"0x290526a710c2bd6abb22d2f2bdeae518938c4a2b\"}]"
    }

    "return a Failure with a function with a wrong return value" in {
      val ps = List.empty[Parameter]
      val senderAddress = "0x00000000000"
      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])

      val f = evmSmartContract.findFunction("wrongType", ps).get
      evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager).isFailure mustEqual true
    }

    "prepare and invoke a view/pure function with a uint8 return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("retrieveUInt8", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x00000000000000000000000000000000000000000000000000000000000000400000000000000000000000000000000000000000000000000000000000000079000000000000000000000000000000000000000000000000000000000000000b48656c6c6f20776f726c64000000000000000000000000000000000000000000"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"uint8\",\"value\":64}]"
    }

    "prepare and invoke a view/pure function with a string,int256 return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("retrieveString", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x0000000000000000000000000000000000000000000000000000000000000040ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffd6000000000000000000000000000000000000000000000000000000000000000b48656c6c6f20776f726c64000000000000000000000000000000000000000000"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"string\",\"value\":\"Hello world\"},{\"index\":1,\"type\":\"int256\",\"value\":-42}]"
    }

    "prepare and invoke a view/pure function with a uint256,bytes return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("retrieve", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x0000000000000000000000000000000000000000000000000000000000000040000000000000000000000000000000000000000000000000000000000000002a00000000000000000000000000000000000000000000000000000000000000063132333435360000000000000000000000000000000000000000000000000000"))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      Json.toJson(result.get.map(r => r.toJson.get)).toString() mustEqual "[{\"index\":0,\"type\":\"bytes\",\"value\":\"0x313233343536\"},{\"index\":1,\"type\":\"uint256\",\"value\":42}]"
    }


    "prepare and invoke a view/pure function with a int256 return value" in {
      val ps = List.empty[Parameter]

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("retrieveInt", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("000000000000000000000000000000000000000000000000000000000000002a"))

      evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
        .get
        .head
        .toJson
        .get
        .toString() mustEqual "{\"index\":0,\"type\":\"int256\",\"value\":42}"
    }

    "find a function for given parameters" in {
      val p = Parameter("res", "bytes", JsString("0x1234"))
      val ps = List(p)
      val evmSmartContract = EvmSmartContract(abi)
      val f = evmSmartContract.findFunction("get_bit_length", ps)
      f.get.name mustEqual "get_bit_length"
      f.get.isConstant mustEqual true
      f.get.toString mustEqual "{\"name\":\"get_bit_length\",\"constant\":\"true\",\"parameters\":[{\"name\":\"val\",\"type\":\"bytes\"}],\"output\":[\"uint256\"]}"
    }

    "Return None if a function does not exist" in {
      val p = Parameter("res", "bytes", JsString("0x1234"))
      val ps = List(p)
      val evmSmartContract = EvmSmartContract(abi)
      val f = evmSmartContract.findFunction("does_not_exist_function", ps)
      f must beNone
    }

    "prepare and invoke a view/pure function" in {
      val p = Parameter("res", "bytes", JsString("0x1234"))
      val ps = List(p)

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("get_bit_length", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address, senderAddress)).thenReturn(Try("0x000000000000000000000000000000000000000000000000000000000000002a"))
      evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
        .get
        .head
        .toJson
        .get
        .toString() mustEqual "{\"index\":0,\"type\":\"uint256\",\"value\":42}"
    }


    "return a failure if parameters are not consistent for the given function" in {

      val invalidPs = List(Parameter("res", "uint256", JsString("0x1234")))
      val ps = List(Parameter("n", "bytes", JsString("0x123456789456")), Parameter("e", "bytes", JsString("0x010010")))

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])

      val f = evmSmartContract.findFunction("addKey", ps).get

      val result = evmSmartContract.prepareAndInvoke(f, 0,invalidPs)(blockchainManager, credentials)
      result.isFailure mustEqual true
    }

    "fail when invoking a view/pure function without node" in {
      val p = Parameter("res", "bytes", JsString("0x1234"))
      val ps = List(p)

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])
      val senderAddress = "0x00000000000"
      val f = evmSmartContract.findFunction("get_bit_length", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invokeView(encodedFunction, abi.address)).thenReturn(Try(throw BlockchainException("Connection unavailable")))

      val result = evmSmartContract.prepareAndInvokeView(f, ps, senderAddress)(blockchainManager)
      result.isFailure mustEqual true
    }

    "fail when invoking a writable function without node" in {
      val p = Parameter("res", "bytes", JsString("0x1234"))
      val ps = List(p)

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])

      val f = evmSmartContract.findFunction("get_bit_length", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invoke(encodedFunction,0, abi.address)(credentials)).thenReturn(Try(throw BlockchainException("Connection unavailable")))

      val result = evmSmartContract.prepareAndInvoke(f,0, ps)(blockchainManager, credentials)
      result.isFailure mustEqual true
    }

    "prepare and invoke a writable function" in {
      val ps = List(Parameter("n", "bytes", JsString("0x123456789456")), Parameter("e", "bytes", JsString("0x010010")))

      val evmSmartContract = EvmSmartContract(abi)
      val blockchainManager = mock(classOf[EthereumManager])

      val f = evmSmartContract.findFunction("addKey", ps).get
      val encodedFunction = FunctionEncoder.encode(evmSmartContract.functionBuilder(ps, f).get)

      when(blockchainManager.invoke(encodedFunction, 0,abi.address)(credentials)).thenReturn(Try("0x1234"))

      val result = evmSmartContract.prepareAndInvoke(f, 0,ps)(blockchainManager, credentials)
      result.get mustEqual "0x1234"
    }

    "return the correct contract address" in {
      val evmSmartContract = EvmSmartContract(abi)
      evmSmartContract.address mustEqual "0x576B0794736878637b829B2EDFAD7C1223CF6F1A"
    }

    "return the correct contract name" in {
      val evmSmartContract = EvmSmartContract(abi)
      evmSmartContract.name mustEqual "my_abi"
    }


  }
}
