package controllers

import applications.dao.ApplicationDao
import applications.model.dao.Application
import com.typesafe.config.ConfigFactory
import common.utils.CryptoUtils
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.PlaySpecification
import smart_contracts.controllers.EventController
import smart_contracts.controllers.blockchain.ethereum.EthereumEventManager
import smart_contracts.dao.{AbiDao, WebhookListenersDao}

import java.util.UUID
import scala.concurrent.{ExecutionContext, Future}

class EventControllerTest extends PlaySpecification with Results {
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))

  val abiDao: AbiDao = mock(classOf[AbiDao])
  val applicationDao: ApplicationDao = mock(classOf[ApplicationDao])
  val ethereumEventManager: EthereumEventManager = mock(classOf[EthereumEventManager])
  val webhookDao: WebhookListenersDao = mock(classOf[WebhookListenersDao])
  val eventController: EventController = new EventController(abiDao, applicationDao, executionContext, configuration, webhookDao, cc)

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
 /* "event  controller" should {
    "getEvents" in {
      val request = FakeRequest(GET, "/api/v3/blockchains/:blockchain/contracts/:contractAddress/events").withHeaders("client-id" -> clientId, "api-key" -> apikey)


      when(ethereumEventManager.getEvents(Map(EthereumEventManager.PARAMS_SINCE -> "0", EthereumEventManager.PARAMS_ADDRESS -> "0x0000"))).thenReturn(Future.successful(List()))

      val result = eventController.getEvents("ethereum", "0x0000", "0").apply(request)

      status(result) mustEqual 200
    }
    "getEvents by name" in {
      val request = FakeRequest(GET, "/api/v3/blockchains/:blockchain/contracts/:contractAddress/events").withHeaders("client-id" -> clientId, "api-key" -> apikey)


      when(ethereumEventManager.getEvents(Map(EthereumEventManager.PARAMS_SINCE -> "0", EthereumEventManager.PARAMS_ADDRESS -> "0x0000"))).thenReturn(Future.successful(List()))

      val result = eventController.getEventsByName("ethereum", "0x0000", "event").apply(request)

      status(result) mustEqual 200
    }
  }*/
}
