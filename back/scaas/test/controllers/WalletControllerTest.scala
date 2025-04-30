package controllers


import better.files.File
import blockchain.ethereum.mocks.EthereumWalletManagerMock
import com.typesafe.config.ConfigFactory
import common.controllers.JsonSanitizer
import common.exceptions.BadRequestException
import common.utils.CryptoUtils
import login.dao.TokensDao
import login.model.Token
import mockws.MockWSHelpers.materializer
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.libs.Files.{SingletonTemporaryFileCreator, TemporaryFile}
import play.api.libs.json.{JsValue, Json, __}
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc.{MultipartFormData, Results}
import play.api.test.{FakeRequest, Helpers, PlaySpecification}
import smart_contracts.model.blockchain.KeyPair
import smart_contracts.model.body.ImportWallet
import users.dao.UserDao
import users.model.dao.User
import wallets.controllers.ethereum.EthereumWalletManager
import wallets.controllers.factory.WalletFactory
import wallets.controllers.{WalletController, WalletManager}
import wallets.dao.WalletDao
import wallets.model.body.ConfigWallet
import wallets.model.dao.Wallet

import java.util.UUID
import java.util.UUID.randomUUID
import scala.concurrent.{ExecutionContext, Future}


class WalletControllerTest extends PlaySpecification with Results {

  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  val walletDBDAO: WalletDao = mock(classOf[WalletDao])
  val conf: Configuration = mock(classOf[Configuration])
  when(conf.get[String]("blockchain.wallet.master-key")).thenReturn("toto")
  when(conf.get[String]("blockchain.ethereum.type")).thenReturn("ethereum")
  when(conf.get[String]("blockchain.alastria.type")).thenReturn("ethereumlegacy")
  val userDao: UserDao = mock(classOf[UserDao])
  val tokenDao: TokensDao = mock(classOf[TokensDao])
  val walletFactory: WalletFactory = mock(classOf[WalletFactory])
  val walletManager: WalletManager = mock(classOf[WalletManager])
  val ethereumWalletManager: EthereumWalletManager = mock(classOf[EthereumWalletManager])
  val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
  val controller = new WalletController(walletDBDAO, userDao, tokenDao, walletFactory, executionContext, conf, Helpers.stubControllerComponents())
  val bc: JsValue = Json.parse("""{"name":"name","password":"password","blockchain":"ethereum"}""")
  val bc2: JsValue = Json.parse("""{"name":"name","password":"password","blockchain":"alastria"}""")
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")
  val sanitize: Wallet => JsValue = JsonSanitizer(__ \ "file", __ \ "salt")("walletId")(Wallet.format)
  val clientId: String = UUID.randomUUID().toString
  val safeToken: String = CryptoUtils(conf).generateSafeToken(24)
  val expirationTimestamp: DateTime = DateTime.now().plusMinutes(90)
  val token: Token = new Token(Some(randomUUID.toString), clientId, safeToken, expirationTimestamp.getMillis)
  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))


  "The WalletController methods" should {
    "Create a Wallet" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/wallets").withBody(bc).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      // when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      val configWallet = ConfigWallet("name", "password", "ethereum")
      when(walletFactory.build(conf, "ethereum")).thenReturn(new EthereumWalletManager())
      when(walletDBDAO.create(any())).thenReturn(Future.successful(wallet))
      val result = controller.createWallet(clientId).apply(request)
      status(result) mustEqual 201
    }
    "Create a Wallet alastria" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/wallets").withBody(bc2).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      // when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "alastria", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      val configWallet = ConfigWallet("name", "password", "alastria")
      when(walletFactory.build(conf, "alastria")).thenReturn(new EthereumWalletManager())
      when(walletDBDAO.create(any())).thenReturn(Future.successful(wallet))
      val result = controller.createWallet(clientId).apply(request)
      status(result) mustEqual 201
    }

    "fail to create Wallet" in {
      val configWallet = ConfigWallet("wallet1", "password", "ethereum")
      val wrongBc: JsValue = Json.parse("""{"blockchain":"ethereum","password":"password"}""")
      val request = FakeRequest(POST, "/wallets").withBody(wrongBc).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "_id")
      when(walletFactory.build(conf, "ethereum")).thenReturn(new EthereumWalletManager())
      when(walletDBDAO.create(any())).thenReturn(Future.failed(BadRequestException("")))

      val result = controller.createWallet("").apply(request)
      (contentAsJson(result) \ "message").get mustEqual Json.toJson("Parsing error")
      status(result) mustEqual 400
    }
    "import a Wallet" in {

      val project: File = File("tests-directory/ressources/alastria.json")
      val jsonData: String =
        """{
          |	"walletPassword": "pass",
          |	"configWallet": {
          |		"name": "wallet1",
          |		"password": "abc",
          |		"blockchain": "ethereum"
          |	}
          |}""".stripMargin

      val projectDirectory: File = File(s"${configuration.get[String]("project.repository")}")
      val myFile: File = File(s"$projectDirectory/alastria.json")
      val jsonPart: Map[String, Seq[String]] = Map("myBody" -> Seq(jsonData))

      val filePart: FilePart[TemporaryFile] = MultipartFormData.FilePart("walletFile", "alastria.json", None, SingletonTemporaryFileCreator.create(myFile.path))
      val dataPart: MultipartFormData[TemporaryFile] = MultipartFormData(jsonPart, Seq(filePart), badParts = Nil)

      if (myFile.notExists) project.copyTo(File(s"${projectDirectory.createIfNotExists(asDirectory = true)}/alastria.json"))


      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/wallets/import").withBody(dataPart).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      val configWallet = ConfigWallet("name", "password", "ethereum")
      val importWallet = ImportWallet("pass", configWallet)
      val walletContent =
        """{
          |    "version": 3,
          |    "id": "ceb5b56c-930a-4879-9f4a-b64246e26b8f",
          |    "address": "e954dedcaf935beca3f78591c7296c9d30a80e66",
          |    "crypto": {
          |        "ciphertext": "e48e685750ceef6b9eb18504cd111f45c859e5db104c30c737daebce58ddb9a4",
          |        "cipherparams": {
          |            "iv": "6af3fcaf86a97364e58d192553b3c146"
          |        },
          |        "cipher": "aes-128-ctr",
          |        "kdf": "scrypt",
          |        "kdfparams": {
          |            "dklen": 32,
          |            "salt": "c88904b14adf31fad1228f07d8be234500a8977031790cd99fa876c19f2c7c37",
          |            "n": 8192,
          |            "r": 8,
          |            "p": 1
          |        },
          |        "mac": "ed69dc0b388d6d9b5b2f93989bf667776f0f455d6b71302246bc50091e29bf87"
          |    }
          |}""".stripMargin

      val ethWMock = new EthereumWalletManagerMock()
      // when(walletFactory.build(conf,"ethereum")).thenReturn(ethWMock)
      when(walletFactory.build(configuration, "ethereum")).thenReturn(new EthereumWalletManager())
      when(ethereumWalletManager.getKeyPair(walletContent, "pass")).thenReturn(KeyPair(BigInt("1"), BigInt("2")))
      when(ethereumWalletManager.importWallet("wallet1", "abc", KeyPair(BigInt("1"), BigInt("2")), "ethereum", "id")(configuration)).thenReturn(wallet)
      when(walletDBDAO.create(any())).thenReturn(Future.successful(wallet))
      val result = controller.importWallet(clientId).apply(request)
      status(result) mustEqual 201
    }


    "getWalletsByUserId" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findBy("userId")(clientId)).thenReturn(Future.successful(Seq(wallet)))

      val result = controller.getWalletsByUserId(clientId).apply(request)
      contentAsJson(result) mustEqual Json.toJson(Seq(wallet).map(sanitize))
      status(result) mustEqual 200

    }
    "fails to getWalletsByUserId" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findBy("userId")(clientId)).thenReturn(Future.failed(new Exception()))

      val result = controller.getWalletsByUserId(clientId).apply(request)

      status(result) mustEqual 500

    }

    "getWallet" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))

      val result = controller.getWallet(clientId, "ethereum", "c1499b34eace8c4a29becab8a9d05b156781f958").apply(request)

      status(result) mustEqual 200

    }
    "fails to getWallet" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.failed(new Exception()))

      val result = controller.getWallet(clientId, "ethereum", "c1499b34eace8c4a29becab8a9d05b156781f958").apply(request)

      status(result) mustEqual 500

    }
    "export wallet" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))

      val result = controller.exportWallet(clientId, "ethereum", "c1499b34eace8c4a29becab8a9d05b156781f958").apply(request)

      status(result) mustEqual 200

    }
    "fails to export wallet" in {
      val request = FakeRequest(GET, "/wallets/id").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)
      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.failed(new Exception()))

      val result = controller.exportWallet(clientId, "ethereum", "c1499b34eace8c4a29becab8a9d05b156781f958").apply(request)

      status(result) mustEqual 500

    }

    "delete wallet" in {
      val request = FakeRequest(GET, "/api/v3/user/blockchains/ethereum/wallets/0xc1499b34eace8c4a29becab8a9d05b156781f958").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)

      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))
      when(walletDBDAO.deleteById("id")).thenReturn(Future.successful((): Unit))

      val result = controller.deleteWallet(clientId, "ethereum", "0xc1499b34eace8c4a29becab8a9d05b156781f958").apply(request)
      status(result) mustEqual 204

    }
    "fails to delete wallet" in {
      val request = FakeRequest(GET, "/api/v3/user/blockchains/ethereum/wallets/0xc1499b34eace8c4a29becab8a9d05b156781f958").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)

      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))
      when(walletDBDAO.deleteById("id")).thenReturn(Future.failed(new Exception()))

      val result = controller.deleteWallet(clientId, "ethereum", "0xc1499b34eace8c4a29becab8a9d05b156781f958").apply(request)
      status(result) mustEqual 500

    }
    "export wallet" in {
      val request = FakeRequest(GET, "/api/v3/user/blockchains/ethereum/wallets/0xc1499b34eace8c4a29becab8a9d05b156781f958").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", clientId)

      when(walletDBDAO.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))
      when(walletDBDAO.deleteById("id")).thenReturn(Future.successful((): Unit))

      val result = controller.exportWallet(clientId, "ethereum", "0xc1499b34eace8c4a29becab8a9d05b156781f958").apply(request)
      status(result) mustEqual 200

    }
  }

}
