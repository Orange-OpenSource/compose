package controllers

import better.files.File
import com.typesafe.config.ConfigFactory
import common.dao.AbstractDao
import common.utils.CryptoUtils
import jdk.internal.org.jline.utils.Log.info
import login.dao.TokensDao
import login.model.Token
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.specs2.execute.Results
import org.web3j.crypto.Credentials
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{ControllerComponents, MultipartFormData}
import play.api.test.Helpers.{GET, POST, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, PlaySpecification}
import smart_contracts.controllers.{ContractsManager, DeploymentController}
import smart_contracts.dao.{ContractDBDAO, OngoingDeploymentDAO}
import smart_contracts.model.dao.{Contract, OngoingDeployment}
import users.dao.UserDao
import users.model.dao.User
import wallets.dao.WalletDao
import wallets.model.dao.Wallet

import java.util.UUID
import java.util.UUID.randomUUID
import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}


class DeploymentControllerTest extends PlaySpecification with Results {

  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
  val projectDirectory: File = File(s"${configuration.get[String]("project.repository")}")
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]


  val contractDBDAO: ContractDBDAO = mock(classOf[ContractDBDAO])
  val dao: AbstractDao[Contract] = contractDBDAO
  val project: File = File("tests-directory/ressources/contracts.zip")
  val tokenDao: TokensDao = mock(classOf[TokensDao])
  val ongoingDeploymentDao: OngoingDeploymentDAO = mock(classOf[OngoingDeploymentDAO])
  val jsonData: String =
    """{
      | "wallet": {
      |		"address": "0xc1499b34eace8c4a29becab8a9d05b156781f958",
      |		"password": "password"
      |	},
      |	"options": {
      |   "network": "ethereum",
      |		"version": "0.8.14",
      |		"evmVersion": "byzantium",
      |		"optimizerRuns": 200
      |	},
      |	"migration": [
      |		{
      |			"name": "Storage",
      |			"links": []
      |		}
      |	]
      |}""".stripMargin
  val myFile: File = File(s"$projectDirectory/contracts.zip")
  val jsonPart: Map[String, Seq[String]] = Map("myBody" -> Seq(jsonData))
  val filePart: FilePart[TemporaryFile] = MultipartFormData.FilePart("projectFile", "contracts.zip", None, SingletonTemporaryFileCreator.create(myFile.path))
  val dataPart: MultipartFormData[TemporaryFile] = MultipartFormData(jsonPart, Seq(filePart), badParts = Nil)
  val userDao: UserDao = mock(classOf[UserDao])
  val contractManager: ContractsManager = mock(classOf[ContractsManager])
  val path: String = "/api/v3/contracts/"
  val walletDao: WalletDao = mock(classOf[WalletDao])
  val contractController = new DeploymentController(contractDBDAO, userDao, tokenDao, ongoingDeploymentDao, contractManager, configuration, walletDao, executionContext, cc)
  val format: OFormat[Contract] = Json.format[Contract]
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")


  val clientId: String = UUID.randomUUID().toString
  val element: Contract = new Contract(Some("_id"), "contract.sol", "ethereum", "contractContent", "projectId", clientId)
  val wallet: Wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
  val safeToken: String = CryptoUtils(configuration).generateSafeToken(24)
  val expirationTimestamp: DateTime = DateTime.now().plusMinutes(90)
  val token: Token = new Token(Some(randomUUID.toString),clientId, safeToken, expirationTimestamp.getMillis)
  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))


  "contract controller" should {

    "create" in {
      if (myFile.notExists) project.copyTo(File(s"${projectDirectory.createIfNotExists(asDirectory = true)}/contracts.zip"))
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withBody(dataPart).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val ongoingDeployment = OngoingDeployment("projectID", "ethereum", clientId, DateTime.now().getMillis.toString)

      when(walletDao.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))

      when(contractManager.uploadFile(any[TemporaryFile], any[JsValue], any[String], any[String], any[String])(any[Credentials])).thenReturn(Future.successful(ListBuffer(element)))
      when(ongoingDeploymentDao.deleteById(any[String])).thenReturn(Future.successful((): Unit))
      when(ongoingDeploymentDao.create(any[OngoingDeployment])).thenReturn(Future.successful(ongoingDeployment))

      val result = contractController.createContracts(clientId).apply(request)

      status(result) mustEqual 200
    }

    "fail to create" in {
      if (myFile.notExists) project.copyTo(File(s"${projectDirectory.createIfNotExists(asDirectory = true)}/contracts.zip"))
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withBody(dataPart).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)


      when(walletDao.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.failed(new Exception()))

      when(contractManager.uploadFile(any[TemporaryFile], any[JsValue], any[String], any[String], any[String])(any[Credentials])).thenReturn(Future.failed(new Exception()))
      when(ongoingDeploymentDao.deleteById(any[String])).thenReturn(Future.successful((): Unit))
      when(ongoingDeploymentDao.create(any[OngoingDeployment])).thenReturn(Future.failed(new Exception()))

      val result = contractController.createContracts(clientId).apply(request)

      status(result) mustEqual 500
    }


    "get ongoing deployment for user " in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(ongoingDeploymentDao.findBy("userId")("_id")).thenReturn(Future.successful(List(OngoingDeployment("projectID", "ethereum", "_id", DateTime.now().getMillis.toString))))
      val result = contractController.getOngoingDeploymentsForUser("_id").apply(request)

      status(result) mustEqual 200
    }
    "fails get ongoing deployment for user " in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(ongoingDeploymentDao.findBy("userId")(clientId)).thenReturn(Future.failed(new Exception()))
      val result = contractController.getOngoingDeploymentsForUser(clientId).apply(request)

      status(result) mustEqual 500
    }
    "get for user " in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(contractDBDAO.findBy("owner")("userid")).thenReturn(Future.successful(List(element)))
      val result = contractController.getForUser("userid").apply(request)

      status(result) mustEqual 200
    }
    "fails get for user " in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(contractDBDAO.findBy("owner")("userid")).thenReturn(Future.failed(new Exception()))
      val result = contractController.getForUser("userid").apply(request)

      status(result) mustEqual 500
    }

    "get deployment " in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(contractDBDAO.findBy("project")("_id")).thenReturn(Future.successful(List(element)))
      val result = contractController.getDeployment("userid", "_id").apply(request)

      status(result) mustEqual 200
    }
    "fails to get deployment " in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/deployments").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(contractDBDAO.findBy("project")("_id")).thenReturn(Future.failed(new Exception()))
      val result = contractController.getDeployment("userid", "_id").apply(request)

      status(result) mustEqual 500
    }
  }
}
