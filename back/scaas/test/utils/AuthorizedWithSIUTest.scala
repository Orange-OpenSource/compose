package utils


import common.controllers.AuthorizedWithSIU
import common.exceptions.{AuthException, DAOException}
import common.utils.{CryptoUtils, TraceUtils}
import login.dao.TokensDao
import login.model.Token
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import org.mockito.Mockito.{mock, when}
import org.specs2.execute.Results
import play.api.Configuration
import play.api.inject.Injector
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AbstractController, Action, AnyContent, ControllerComponents}
import play.api.test.{FakeRequest, PlaySpecification}
import users.dao.UserDao
import users.model.dao.User

import java.util.UUID
import java.util.UUID.randomUUID
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AuthorizedWithSIUTest extends PlaySpecification with Results {

  sequential
  
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val userDao: UserDao = mock(classOf[UserDao])
  val tokenDao: TokensDao = mock(classOf[TokensDao])
  val configuration: Configuration = mock(classOf[Configuration])

  private class DummyTestController @Inject()(cc: ControllerComponents,
                                              userDao: UserDao,
                                              ec: ExecutionContext
                                             ) extends AbstractController(cc) with TraceUtils {
    private val authorization: AuthorizedWithSIU = AuthorizedWithSIU(userDao, tokenDao, cc, ec)

    def foo(): Action[AnyContent] = authorization.authorized(List("USER"))(parse.empty) {
      entering("foo()")
      Ok
    }
  }


  "AuthorizedWithSIU" should {
    "authorize" in {
      val dummyController = new DummyTestController(cc, userDao, executionContext)

      val clientId = UUID.randomUUID().toString
      val safeToken = CryptoUtils(configuration).generateSafeToken(24)
      val expirationTimestamp = DateTime.now().plusMinutes(90)
      val token: Token = new Token(Some(randomUUID.toString),clientId, safeToken, expirationTimestamp.getMillis)
      when(userDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("USER"), "compagnyId")
          )
        )
      when(tokenDao
        .findOneBy("userToken")(token.userToken))
        .thenReturn(Future.successful(token))

      val request = FakeRequest(GET, "")
        .withHeaders(
          "user-id" -> clientId,
          "user-token" -> token.userToken
        )

      val result = dummyController.foo().apply(request)

      status(result) mustEqual 200
    }

    "not authorize if the headers are not present" in {
      val dummyController = new DummyTestController(cc, userDao, executionContext)

      val clientId = UUID.randomUUID().toString


      when(userDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("USER"), "compagnyId")
          )
        )

      val request = FakeRequest(GET, "")

      dummyController.foo().apply(request) must throwA(AuthException("id not provided"))
    }

    "not authorize if the clientID does not exist" in {

      val dummyController = new DummyTestController(cc, userDao, executionContext)

      val impostorClientId = UUID.randomUUID().toString

      val clientId = UUID.randomUUID().toString
      val apikey = CryptoUtils(configuration).generateSafeToken(16)
      val salt = BCrypt.gensalt()

      val hashedApiKey = BCrypt.hashpw(apikey, salt)
      when(userDao
        .findById(clientId))
        .thenReturn(
          Future.successful(
            User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("USER"), "compagnyId")
          )
        )


      when(userDao.findById(impostorClientId))
        .thenReturn(Future.failed(
          DAOException("Dummy error")
        ))


      val request = FakeRequest(GET, "")
        .withHeaders(
          "USER_ID" -> impostorClientId,
          "SIU_Key" -> apikey)

      dummyController.foo().apply(request) must throwA(AuthException("id not provided"))

    }


  }

}
