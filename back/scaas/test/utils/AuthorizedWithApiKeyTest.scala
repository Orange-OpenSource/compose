package utils

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.controllers.AuthorizedWithApiKey
import common.exceptions.DAOException
import common.utils.{CryptoUtils, TraceUtils}
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito.{mock, when}
import org.specs2.execute.Results
import play.api.Configuration
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, PlaySpecification}

import java.util.UUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorizedWithApiKeyTest extends PlaySpecification with Results {

  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val machineDao: ApplicationDao = mock(classOf[ApplicationDao])
  val configuration: Configuration = mock(classOf[Configuration])

  private class DummyTestController @Inject()(cc: ControllerComponents,
                                              machineDBDAO: ApplicationDao,
                                              ec: ExecutionContext
                                             ) extends AbstractController(cc) with TraceUtils {
    private val authorization: AuthorizedWithApiKey = AuthorizedWithApiKey(machineDBDAO, cc, ec)

    def foo(): Action[AnyContent] = authorization.authorized(parse.empty) {
      entering("foo()")
      Ok
    }
  }


  "AuthorizedWithApiKey" should {
    "authorize" in {
      val dummyController = new DummyTestController(cc, machineDao, executionContext)

      val clientId = UUID.randomUUID().toString
      val apikey = CryptoUtils(configuration).generateSafeToken(20)
      val salt = BCrypt.gensalt()

      val hashedApiKey = BCrypt.hashpw(apikey, salt)
      when(machineDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            Application(
              Some(clientId),
              "dummymachine",
              Some("127.0.0.1"),
              hashedApiKey,
              salt,
              "ethereum",
              "walletId"
            )
          )
        )

      val request = FakeRequest(GET, "")
        .withHeaders(
          "client-id" -> clientId,
          "api-key" -> apikey
        )

      val result = dummyController.foo().apply(request)
      status(result) mustEqual 200
    }

    "not authorize if the headers are not present" in {
      val dummyController = new DummyTestController(cc, machineDao, executionContext)

      val clientId = UUID.randomUUID().toString
      val apikey = CryptoUtils(configuration).generateSafeToken(16)
      val salt = BCrypt.gensalt()

      val hashedApiKey = BCrypt.hashpw(apikey, salt)
      when(machineDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            Application(
              Some(clientId),
              "dummymachine",
              Some("1.1.1.1"),
              hashedApiKey,
              salt,
              "ethereum",
              "walletId"
            )
          )
        )

      val request = FakeRequest(GET, "")

      val result = dummyController.foo().apply(request)
      status(result) mustEqual 403
    }

    "not authorize if the clientID does not exist" in {

      val dummyController = new DummyTestController(cc, machineDao, executionContext)

      val impostorClientId = UUID.randomUUID().toString

      val clientId = UUID.randomUUID().toString
      val apikey = CryptoUtils(configuration).generateSafeToken(16)
      val salt = BCrypt.gensalt()

      val hashedApiKey = BCrypt.hashpw(apikey, salt)
      when(machineDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            Application(
              Some(clientId),
              "dummymachine",
              Some("1.1.1.1"),
              hashedApiKey,
              salt,
              "ethereum",
              "walletId"
            )
          )
        )


      when(machineDao.findById(impostorClientId))
        .thenReturn(Future.failed(
          DAOException("Dummy error")
        ))


      val request = FakeRequest(GET, "")
        .withHeaders(
          "CLIENT_ID" -> impostorClientId,
          "API_KEY" -> apikey)

      val result = dummyController.foo().apply(request)
      status(result) mustEqual 403
    }

    "not authorize if the API KEY is incorrect" in {

      val dummyController = new DummyTestController(cc, machineDao, executionContext)

      val clientId = UUID.randomUUID().toString
      val apikey = CryptoUtils(configuration).generateSafeToken(16)
      val salt = BCrypt.gensalt()
      val impostorApiKey = CryptoUtils(configuration).generateSafeToken(16)

      val hashedApiKey = BCrypt.hashpw(apikey, salt)
      when(machineDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            Application(
              Some(clientId),
              "dummymachine",
              Some("1.1.1.1"),
              hashedApiKey,
              salt,
              "ethereum",
              "walletId"
            )
          )
        )

      val request = FakeRequest(GET, "")
        .withHeaders(
          "CLIENT_ID" -> clientId,
          "API_KEY" -> impostorApiKey)

      val result = dummyController.foo().apply(request)
      status(result) mustEqual 403
    }

  }

}
