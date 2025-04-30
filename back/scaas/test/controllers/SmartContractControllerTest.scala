package controllers

import applications.dao.ApplicationDao
import applications.model.dao.Application
import blockchain.ethereum.mocks.EthereumManagerMock
import com.typesafe.config.ConfigFactory
import common.utils.CryptoUtils
import mockws.MockWSHelpers.materializer
import org.mindrot.jbcrypt.BCrypt
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.web3j.crypto.Credentials
import play.api.Configuration
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.{FakeRequest, PlaySpecification}
import play.api.test.Helpers.{POST, PUT, defaultAwaitTimeout, status}
import smart_contracts.controllers.SmartContractController
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.controllers.blockchain.ethereum.{EthereumEventManager, EthereumManager}
import smart_contracts.controllers.blockchain.factory.BlockchainFactory
import smart_contracts.controllers.vm.evm.EvmSmartContract
import smart_contracts.dao.AbiDao
import smart_contracts.model.blockchain.ReturnValue
import smart_contracts.model.blockchain.evm.Parameter
import smart_contracts.model.body.*
import smart_contracts.model.dao.Abi
import wallets.dao.WalletDao
import wallets.model.dao.Wallet

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

class SmartContractControllerTest extends PlaySpecification with Results {
  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
  val ethm: EthereumManager = mock(classOf[EthereumManager])
  val abiDao: AbiDao = mock(classOf[AbiDao])
  val walletDao: WalletDao = mock(classOf[WalletDao])
  val applicationDao: ApplicationDao = mock(classOf[ApplicationDao])
  val ethereumEventManager: EthereumEventManager = mock(classOf[EthereumEventManager])
  val sc: EvmSmartContract = mock(classOf[EvmSmartContract])
  val bcFactory: BlockchainFactory = mock(classOf[BlockchainFactory])
  val body: JsValue = Json.parse("""{"wallet":{"address":"0x0000"},"parameters":[{"name":"param1","type":"string","value":"hello"}]}""")
  val smartContractController: SmartContractController = new SmartContractController(applicationDao , abiDao , walletDao, bcFactory, configuration , executionContext , cc)
  val clientId: String = UUID.randomUUID().toString
  val apikey: String = CryptoUtils(configuration).generateSafeToken(20)
  val salt: String = BCrypt.gensalt()
  val hashedApiKey: String = BCrypt.hashpw(apikey, salt)
  val app: Application = Application(
    Some(clientId),
    "dummymachine",
    Some("127.0.0.1"),
    hashedApiKey,
    salt,
    "ethereum",
    "walletId"
  )
  when(applicationDao.findById(clientId)).thenReturn(Future.successful(app))

  val addressInfos: AddressInfos = AddressInfos("0xc1499b34eace8c4a29becab8a9d05b156781f958")
  val parameter: Parameter = Parameter("b", "uint256", Json.toJson("10"))
  val readFunctionParameters: ReadFunctionParameters = ReadFunctionParameters(Some(addressInfos), List(parameter))
  val transacOption: TransactionOptions = TransactionOptions(4042055464L,2500000000L,4042055464L)
  val functionParameters : FunctionParameters = FunctionParameters(Some(transacOption),"Password123!",Some(BigInt(0)),List(parameter))
  "smartcontract  controller" should {
    "pureOrViewFunction" in {
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)

      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withBody(readFunctionParameters).withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val wallet = Wallet(Some("walletId"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1","ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "_id")
      val abi: Abi = new Abi(Some("_id"),"abi1","ethereum","[\n\t\t\t{\n\t\t\t\t\"inputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"constructor\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"anonymous\": false,\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"address\",\n\t\t\t\t\t\t\"name\": \"sender\",\n\t\t\t\t\t\t\"type\": \"address\"\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"amount\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"NewStore\",\n\t\t\t\t\"type\": \"event\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"a\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"set\",\n\t\t\t\t\"outputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"function\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"b\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"add\",\n\t\t\t\t\"outputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"stateMutability\": \"view\",\n\t\t\t\t\"type\": \"function\",\n\t\t\t\t\"constant\": true\n\t\t\t}\n\t\t]","c1499b34eace8c4a29becab8a9d05b156781f958","_id")
      val returnedValue: ReturnValue = ReturnValue(1,"add","")
      when(walletDao.findById("walletId")).thenReturn(Future.successful(wallet))
      when(abiDao.findBy("address")("0xc1499b34eace8c4a29becab8a9d05b156781f958")).thenReturn(Future.successful(List(abi)))
      //when(sc.prepareAndInvokeView(any[smart_contracts.model.blockchain.Function],List(any[Parameter]),any[String])(any[BlockchainManager])).thenReturn(Success(List(returnedValue)))
      val result = smartContractController.pureOrViewFunction("ethereum", "0xc1499b34eace8c4a29becab8a9d05b156781f958", "add").apply(request)
      status(result) mustEqual 200
    }
    "async function" in {
      val request = FakeRequest(POST, "/api/v3/blockchains/ethereum/contracts/0x21f8Ad2c5bE7D67669243d3f6C43D00dBD2aC018/functions/function/eval").withBody(functionParameters).withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val wallet = Wallet(Some("walletId"), "0x21f8Ad2c5bE7D67669243d3f6C43D00dBD2aC018", "wallet1", "ethereum", "3e004a9b67a4851930904a4dff731036", "{\"address\":\"21f8ad2c5be7d67669243d3f6c43d00dbd2ac018\",\"id\":\"d138a5c2-6833-414e-8f06-9db99bc015a7\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4c31e0a7da6aa01820f4c0ece8f36bdb4dbca84c462bd4c73345cf11e1f6aef7\",\"cipherparams\":{\"iv\":\"4c56bd93e99c4a9eb41a7f5973d17b42\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"55a497b4aecd4781107515f9320815b4d1c93e02fffd8bc625d8b2e583be8bca\"},\"mac\":\"573694a9359c89e9325e0b3d7a63f2c9bb87bf5c5898aa9fdd9aca755980473e\"}}", "_id")
      val abi: Abi = new Abi(Some("_id"), "abi1", "ethereum", "[\n\t\t\t{\n\t\t\t\t\"inputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"constructor\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"anonymous\": false,\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"address\",\n\t\t\t\t\t\t\"name\": \"sender\",\n\t\t\t\t\t\t\"type\": \"address\"\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"amount\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"NewStore\",\n\t\t\t\t\"type\": \"event\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"a\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"set\",\n\t\t\t\t\"outputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"function\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"b\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"add\",\n\t\t\t\t\"outputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"stateMutability\": \"view\",\n\t\t\t\t\"type\": \"function\",\n\t\t\t\t\"constant\": true\n\t\t\t}\n\t\t]", "c1499b34eace8c4a29becab8a9d05b156781f958", "_id")
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)

      when(walletDao.findById("walletId")).thenReturn(Future.successful(wallet))
      when(abiDao.findBy("address")("0xc1499b34eace8c4a29becab8a9d05b156781f958")).thenReturn(Future.successful(List(abi)))
      val smc = mock(classOf[SmartContractController])

      when(sc.prepareAndInvoke(any[smart_contracts.model.blockchain.Function],any[BigInt] ,List(any[Parameter]))(any[BlockchainManager],any[Credentials])).thenReturn(Success("test"))
      val result = smartContractController.asyncFunction("ethereum", "0xc1499b34eace8c4a29becab8a9d05b156781f958", "set").apply(request)
      status(result) mustEqual 200
    }


    "get block number" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum",configuration,"ethereum")).thenReturn(ethMock)
      when(ethm.getBlockNumber).thenReturn("1")
      val result = smartContractController.getBlockNumber("ethereum").apply(request)
      println(result)
      status(result) mustEqual 200
    }
    "get block by number" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)
      val block: Block = Block(Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List("")),Some(List("")),Some(""))
      when(ethm.getBlockByNumber("1")).thenReturn(block)
      val result = smartContractController.getBlockByNumber("ethereum","1").apply(request)
      status(result) mustEqual 200
    }

    "get block by hash" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)
      val block: Block = Block(Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List("")),Some(List("")),Some(""))
      when(ethm.getBlockByHash("1")).thenReturn(block)
      val result = smartContractController.getBlockByHash("ethereum", "1").apply(request)
      status(result) mustEqual 200
    }
    "get code at" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)
      when(ethm.getCodeAt("0x0000")).thenReturn("")
      val result = smartContractController.getCodeAt("ethereum", "0x0000").apply(request)
      status(result) mustEqual 200
    }
    "get protocol version" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)
      when(ethm.getProtocolVersion).thenReturn("")
      val result = smartContractController.getProtocolVersion("ethereum").apply(request)
      status(result) mustEqual 200
    }
    "get transaction receipt" in {
      val request = FakeRequest(PUT, "/api/v3/blockchains/ethereum/contracts/0x0000/functions/function/read").withHeaders("client-id" -> clientId, "api-key" -> apikey)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)

      val log: Log = new smart_contracts.model.body.Log(
        Some(false),
        Some( "16"),
        Some( "4"),
        Some("0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc"),
        Some("0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1"),
        Some("8726746"),
        Some("0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8"),
        Some("0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000"),
        Some("1"),
        Some(List("0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777")))
      val transaction : Transaction = Transaction(Some("0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc"),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List(log)),Some(""),Some(""),Some(""),Some(""))
      when(ethm.transactionReceipt("0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc")).thenReturn(Success(transaction))
      val result = smartContractController.transactionReceipt("ethereum","0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc").apply(request)
      status(result) mustEqual 200
    }


  }

}
