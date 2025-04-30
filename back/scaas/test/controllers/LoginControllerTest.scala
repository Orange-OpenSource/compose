package controllers

import common.exceptions.LoginException
import login.controllers.{LoginController, LoginManager}
import login.model.{LoginInput, Token}
import org.mockito.Mockito.{mock, when}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.Helpers.{POST, defaultAwaitTimeout, status}
import play.api.test.{FakeRequest, PlaySpecification}
import specs2.arguments.sequential
import users.model.body.UserInput
import users.model.dao.User

import scala.concurrent.{ExecutionContext, Future}

class LoginControllerTest extends PlaySpecification with Results {

  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  val user: User = new User(Some("_id"), "name", "familyname", "test@compose.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  val loginInputBody: JsValue = Json.parse("""{"email":"test@compose.com","password":"Password1!"}""")
  val loginInput: LoginInput = LoginInput("test@compose.com", "Password1!")
  val userInput: UserInput = new UserInput("name", "familyname", "0260566058", "test@test.com", "password", List("USER"), "companyId")
  val token: Token = new Token(Some("_id"),"_id", "token", 500000)
  val loginManager: LoginManager = mock(classOf[LoginManager])
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val loginController: LoginController = new LoginController(loginManager, executionContext, cc)
  "LoginControllerTest" should {
    "Login" in {

      val request = FakeRequest(POST, "/api/v3/login").withBody(loginInputBody)
      when(loginManager.isLoginOk(loginInput)).thenReturn(Future.successful(token))
      val result = loginController.login().apply(request)
      status(result) mustEqual 200
    }
    "Login wrong body" in {

      val request = FakeRequest(POST, "/api/v3/login").withBody(Json.parse("""{}"""))
      when(loginManager.isLoginOk(loginInput)).thenReturn(Future.successful(token))
      val result = loginController.login().apply(request)
      status(result) mustEqual 400
    }
    "Login wrong login/password" in {
      val loginInputBody2: JsValue = Json.parse("""{"email":"test@compose.com","password":"Pord1!"}""")
      val request = FakeRequest(POST, "/api/v3/login").withBody(loginInputBody2)
      val loginInput2: LoginInput = LoginInput("test@compose.com", "Pord1!")
      when(loginManager.isLoginOk(loginInput2)).thenReturn(Future.failed(LoginException("wrong login/password")))
      val result = loginController.login().apply(request)
      status(result) mustEqual 403
    }
  }
}
