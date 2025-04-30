package controllers

import blockchain.ethereum.mocks.EthereumManagerMock
import com.typesafe.config.ConfigFactory
import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import common.utils.CryptoUtils
import login.dao.TokensDao
import login.model.Token
import org.joda.time.DateTime
import mockws.MockWSHelpers.materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import smart_contracts.controllers.AbiController
import smart_contracts.controllers.blockchain.factory.BlockchainFactory
import smart_contracts.dao.AbiDao
import smart_contracts.model.body.AbiInput
import smart_contracts.model.dao.Abi
import users.dao.UserDao
import users.model.dao.User

import java.util.UUID
import java.util.UUID.randomUUID
import scala.concurrent.{ExecutionContext, Future}

class AbiControllerTest extends AbstractIdentityControllerTest[Abi]  {


  override val sanitize: Abi => JsValue = JsonSanitizer()("contractId")(Abi.format)
  override val element: Abi = new Abi(Some("_id"), "abi1", "ethereum", "abi", "0x0000", "_id")
  val elementInput: AbiInput = new AbiInput("abi1", "[\n\t\t\t{\n\t\t\t\t\"inputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"constructor\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"anonymous\": false,\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"address\",\n\t\t\t\t\t\t\"name\": \"sender\",\n\t\t\t\t\t\t\"type\": \"address\"\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"amount\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"NewStore\",\n\t\t\t\t\"type\": \"event\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"a\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"set\",\n\t\t\t\t\"outputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"function\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"b\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"add\",\n\t\t\t\t\"outputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"stateMutability\": \"view\",\n\t\t\t\t\"type\": \"function\",\n\t\t\t\t\"constant\": true\n\t\t\t}\n\t\t]", "ethereum")
  override val newElement: Abi = new Abi(Some("_id2"), "abi2", "ethereum", "abi", "0x0000", "_id")
  val newElementInput: AbiInput = new AbiInput("abi2", "[\n\t\t\t{\n\t\t\t\t\"inputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"constructor\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"anonymous\": false,\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"address\",\n\t\t\t\t\t\t\"name\": \"sender\",\n\t\t\t\t\t\t\"type\": \"address\"\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"amount\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"NewStore\",\n\t\t\t\t\"type\": \"event\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"a\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"set\",\n\t\t\t\t\"outputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"function\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"b\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"add\",\n\t\t\t\t\"outputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"stateMutability\": \"view\",\n\t\t\t\t\"type\": \"function\",\n\t\t\t\t\"constant\": true\n\t\t\t}\n\t\t]", "ethereum")
  override val elementId: String = "_id"
  override val body: JsValue = Json.parse(
    """{
      |  "name": "store contract 1",
      |  "abi": "[\n\t\t\t{\n\t\t\t\t\"inputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"constructor\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"anonymous\": false,\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"address\",\n\t\t\t\t\t\t\"name\": \"sender\",\n\t\t\t\t\t\t\"type\": \"address\"\n\t\t\t\t\t},\n\t\t\t\t\t{\n\t\t\t\t\t\t\"indexed\": false,\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"amount\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"NewStore\",\n\t\t\t\t\"type\": \"event\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"a\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"set\",\n\t\t\t\t\"outputs\": [],\n\t\t\t\t\"stateMutability\": \"nonpayable\",\n\t\t\t\t\"type\": \"function\"\n\t\t\t},\n\t\t\t{\n\t\t\t\t\"inputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"b\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"name\": \"add\",\n\t\t\t\t\"outputs\": [\n\t\t\t\t\t{\n\t\t\t\t\t\t\"internalType\": \"uint256\",\n\t\t\t\t\t\t\"name\": \"\",\n\t\t\t\t\t\t\"type\": \"uint256\"\n\t\t\t\t\t}\n\t\t\t\t],\n\t\t\t\t\"stateMutability\": \"view\",\n\t\t\t\t\"type\": \"function\",\n\t\t\t\t\"constant\": true\n\t\t\t}\n\t\t]",
      |	"blockchain":"ethereum"
      |
      |}""".stripMargin)
  val bcFactory: BlockchainFactory = mock(classOf[BlockchainFactory])
 
  val abiDao: AbiDao = mock(classOf[AbiDao])
  
  override val abstractUserController: AbstractIdentityController[Abi] = new AbiController(abiDao, userDBDAO, tokenDao, configuration, bcFactory,executionContext, cc)
  override val path: String = "/api/v3/abi/"
  override val dao: AbstractDao[Abi] = abiDao
  val abiController: AbiController = new AbiController(abiDao, userDBDAO, tokenDao, configuration,bcFactory, executionContext, cc)
  override val format: OFormat[Abi] = Json.format[Abi]
  
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_id"), List("SUPER", "ADMIN", "USER"), "compagnyId")


  "AbiControllerTest" should {
    "retrieveAbis" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")("_id")).thenReturn(Future.successful(List(element)))

      val result = abiController.retrieveAbis("_id").apply(request)

      status(result) mustEqual 200
    }
    "fails to retrieves abi" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")("_id")).thenReturn(Future.failed(new Exception()))

      val result = abiController.retrieveAbis("_id").apply(request)

      status(result) mustEqual 500

    }

    "retrieveAbis for blockchain" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")(clientId)).thenReturn(Future.successful(List(element)))

      val result = abiController.retrieveAbisForBlockchain("ethereum").apply(request)


      status(result) mustEqual 200
    }
    "fails to retrieves abi for blockchain" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")(clientId)).thenReturn(Future.failed(new Exception()))

      val result = abiController.retrieveAbisForBlockchain("ethereum").apply(request)

      status(result) mustEqual 500

    }

    "retrieve" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")("_id")).thenReturn(Future.successful(List(element)))

      val result = abiController.retrieve("_id", "0x0000").apply(request)

      status(result) mustEqual 200
    }
    "fails to retrieve" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("userId")("_id")).thenReturn(Future.failed(new Exception()))

      val result = abiController.retrieve("_id", "0x0000").apply(request)

      status(result) mustEqual 500

    }
    "create abi" in {

      val request = FakeRequest(GET, path).withBody(Json.toJson(elementInput)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.create(any[Abi])).thenReturn(Future.successful(element))

      val result = abiController.createAbi("_id", "0x0000").apply(request)

      status(result) mustEqual 201

    }
    "fails to create abi" in {

      val request = FakeRequest(GET, path).withBody(Json.toJson(elementInput)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.create(element)).thenReturn(Future.failed(new Exception()))

      val result = abiController.retrieve("_id", "0x0000").apply(request)

      status(result) mustEqual 500
    }
    "delete abi" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("address")("0x0000")).thenReturn(Future.successful(List(element)))
      when(abiDao.deleteById("_id")).thenReturn(Future.successful((): Unit))
      val result = abiController.deleteAbi("_id", "ethereum", "0x0000").apply(request)

      status(result) mustEqual 204

    }
    "fails to delete abi" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.findBy("address")("0x0000")).thenReturn(Future.successful(List(element)))
      when(abiDao.deleteById("_id")).thenReturn(Future.successful((): Unit))
      val result = abiController.retrieve("_id", "0x0000").apply(request)

      status(result) mustEqual 500

    }
    "delete abi by id" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.deleteById("_id")).thenReturn(Future.successful((): Unit))
      val result = abiController.deleteById("_id", "_id").apply(request)

      status(result) mustEqual 204

    }
    "fails to delete abi by id" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(abiDao.deleteById("_id")).thenReturn(Future.failed(new Exception()))

      val result = abiController.deleteById("_id", "_id").apply(request)

      status(result) mustEqual 500

    }
    "get active blockchain" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      //when(configuration.getPrototypedSeq("blockchain.active"))

      val result = abiController.getActiveBlockchains.apply(request)


      status(result) mustEqual 200

    }

    "get blockchain" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val ethMock = new EthereumManagerMock()

      when(bcFactory.build("ethereum", configuration, "ethereum")).thenReturn(ethMock)

      val result = abiController.getBlockchain("ethereum").apply(request)

      status(result) mustEqual 200

    }

  }

}
