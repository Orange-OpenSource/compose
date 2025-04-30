package managers

import credentials.dao.CredentialDao
import credentials.model.dao.Credential
import login.controllers.LoginManager
import login.dao.TokensDao
import login.model.{LoginInput, Token}
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.test.PlaySpecification
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class LoginManagerTest extends PlaySpecification {
  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  val conf: Configuration = mock(classOf[Configuration])
  val user: User = new User(Some("_id"), "name", "familyname", "test@compose.com", "0600000000", "idCred", List("machine1"), List("admin"), "compagnyId")
  val loginInput: LoginInput = LoginInput("test@compose.com", "Password1!")
  val badLoginInput: LoginInput = LoginInput("test@compose.com", "Password1")
  val cred = new Credential(Some("idCred"), "$2a$10$GSbeCBYFaT88pLHq6OEbDe", "$2a$10$GSbeCBYFaT88pLHq6OEbDeUX0ppj08DiGWErFuX5CnXajpdo1pWQ.", Some("otp"))
  val credentialDao: CredentialDao = mock(classOf[CredentialDao])
  val userDao: UserDao = mock(classOf[UserDao])
  val tokenDao: TokensDao = mock(classOf[TokensDao])
  val loginManager = new LoginManager(credentialDao, userDao, tokenDao, conf, executionContext)
  val token = new Token(Some("_id"),"_id", "token", DateTime.now().plusMinutes(90).getMillis)
  "LoginManagerTest" should {
    "is login ok" in {
      when(userDao.findOneBy("email")("test@compose.com")).thenReturn(Future.successful(user))
      when(credentialDao.findById(any[String])).thenReturn(Future.successful(cred))
      when(tokenDao.create(any[Token])).thenReturn(Future.successful(token))
      val result = Await.result(loginManager.isLoginOk(loginInput), Duration.Inf)
      result mustEqual token
    }
    "is login not Ok" in {
      when(userDao.findOneBy("email")("test@compose.com")).thenReturn(Future.successful(user))
      when(credentialDao.findById(any[String])).thenReturn(Future.successful(cred))
      Await.result(loginManager.isLoginOk(badLoginInput), Duration.Inf) must throwA[Exception]("Invalid password")

    }
  }
}
