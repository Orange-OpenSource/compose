package controllers

import com.typesafe.config.ConfigFactory
import common.controllers.AbstractIdentityController
import common.dao.AbstractDao
import common.exceptions.DAOException
import common.utils.CryptoUtils
import login.dao.TokensDao
import login.model.Token
import org.joda.time.DateTime
import org.mockito.Mockito.{mock, when}
import org.specs2.execute.Results
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsDefined, JsValue, Json, OFormat}
import play.api.mvc.ControllerComponents
import play.api.test.{FakeRequest, PlaySpecification}
import users.dao.UserDao
import users.model.dao.User

import java.util.UUID
import java.util.UUID.randomUUID
import scala.concurrent.{ExecutionContext, Future}

abstract class AbstractIdentityControllerTest[T] extends PlaySpecification with Results {

  sequential
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  val element: T
  val newElement: T
  val elementId: String
  val dao: AbstractDao[T]
  val c: AbstractIdentityController[?] = mock(classOf[AbstractIdentityController[?]])
  val body: JsValue
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  implicit val format: OFormat[T]
  val abstractUserController: AbstractIdentityController[T]
  val path: String

  val sanitize: T => JsValue

  val configuration = new Configuration(ConfigFactory.load("application.test.conf"))
  val tokenDao: TokensDao = mock(classOf[TokensDao])
  val userDBDAO: UserDao = mock(classOf[UserDao])
  val clientId: String = UUID.randomUUID().toString
  val safeToken: String = CryptoUtils(configuration).generateSafeToken(24)
  val expirationTimestamp: DateTime = DateTime.now().plusMinutes(90)
  val token = new Token(Some(randomUUID.toString),clientId, safeToken, expirationTimestamp.getMillis)

  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDBDAO.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))

  "AbstractIdentityControllerTest" should {

    "Count" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(dao.count()).thenReturn(Future.successful(1L))
      val result = abstractUserController.count.apply(request)
      contentAsJson(result) mustEqual Json.toJson(1)
      status(result) mustEqual 200
    }
    "fails to Count" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(dao.count()).thenReturn(Future.failed(new Exception()))
      val result = abstractUserController.count.apply(request)

      status(result) mustEqual 500
    }

    "getOne" in {


      val request = FakeRequest(GET, path + elementId).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.findById(elementId)).thenReturn(Future.successful(element))
      val result = abstractUserController.getOne(elementId).apply(request)
      contentAsJson(result) mustEqual Json.toJson(sanitize(element))
      status(result) mustEqual 200
    }
    "fail to getOne" in {

      val request = FakeRequest(GET, path + elementId).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.findById(elementId)).thenReturn(Future.failed(DAOException("")))
      val result = abstractUserController.getOne(elementId).apply(request)
      (contentAsJson(result) \ "message").get mustEqual Json.toJson("Dao Exception")
      status(result) mustEqual 500
    }


    "delete" in {


      val request = FakeRequest(DELETE, path + elementId).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.deleteById(elementId)).thenReturn(Future.successful((): Unit))
      val result = abstractUserController.delete(elementId).apply(request)
      status(result) mustEqual 204
    }
    "fail to delete" in {


      val request = FakeRequest(DELETE, path + elementId).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.deleteById(elementId)).thenReturn(Future.failed(DAOException("")))
      val result = abstractUserController.delete(elementId).apply(request)
      (contentAsJson(result) \ "message").get mustEqual Json.toJson("Dao Exception")
      status(result) mustEqual 500
    }

    "getAll" in {


      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.findAll()).thenReturn(Future.successful(List(element, newElement)))
      val result = abstractUserController.getAll.apply(request)

      contentAsJson(result) mustEqual Json.toJson(List(element, newElement).map(sanitize))
      status(result) mustEqual 200
    }
    "fail to getAll" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(dao.findAll()).thenReturn(Future.failed(DAOException("")))
      val result = abstractUserController.getAll.apply(request)

      (contentAsJson(result) \ "message") mustEqual JsDefined(Json.toJson("Dao Exception"))
      status(result) mustEqual 500
    }

  }


}
