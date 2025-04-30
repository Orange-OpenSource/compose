package controllers

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import credentials.controllers.{CredentialController, CredentialsManager}
import credentials.dao.CredentialDao
import credentials.model.body.CredentialInput
import credentials.model.dao.Credential
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat, __}
import play.api.test.FakeRequest
import play.api.test.Helpers.{POST, PUT, contentAsJson, defaultAwaitTimeout, status}
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.Future

class CredentialControllerTest extends AbstractIdentityControllerTest[Credential] {
  override val element: Credential = new Credential(Some("_id1"), "salt", "Password1!", Some("otp"))
  val elementInput: CredentialInput = new CredentialInput("Password1!", Some("otp"))
  override val newElement: Credential = new Credential(Some("_id2"), "salt", "Password2!", Some("otp"))
  val newElementInput: CredentialInput = new CredentialInput("Password2!", Some("otp"))
  override val elementId: String = "_id2"
  val credentialDBDAO: CredentialDao = mock(classOf[CredentialDao])
  val credentialManager: CredentialsManager = mock(classOf[CredentialsManager])
  override val dao: AbstractDao[Credential] = credentialDBDAO
  val userDao: UserDao = mock(classOf[UserDao])
  override val body: JsValue = Json.parse("""{"password":"Password2!","otp":"otp"}""")

  override val abstractUserController: AbstractIdentityController[Credential] = new CredentialController(credentialDBDAO, credentialManager, userDao, tokenDao, executionContext, cc)
  override val path: String = "/api/v3/credentials/"
  val credentialController = new CredentialController(credentialDBDAO, credentialManager, userDao, tokenDao, executionContext, cc)
  override implicit val format: OFormat[Credential] = Json.format[Credential]
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")
  val sanitize: Credential => JsValue = JsonSanitizer(__ \ "otp", __ \ "salt", __ \ "hashedPassword")("credentialId")

  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))

  "Credential Controller" should {
    "update" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(credentialManager.updateCredential(any[String], any[CredentialInput])).thenReturn(Future.successful(newElement))
      val result = credentialController.update(elementId).apply(request)
      status(result).mustEqual(200)
    }
    "fail to update" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(credentialManager.updateCredential(any[String], any[CredentialInput])).thenReturn(Future.failed(new Exception("test")))
      val result = credentialController.update(elementId).apply(request)
      (contentAsJson(result) \ "message").get.mustEqual(Json.toJson("Internal server error"))
      status(result).mustEqual(500)
    }
    "fail to update with wrong body" in {
      val request = FakeRequest(PUT, path + elementId).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val result = credentialController.update(elementId).apply(request)

      status(result).mustEqual(400)
    }


    "create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(credentialManager.createFromPassword(any[String])).thenReturn(Future.successful(element))
      val result = credentialController.create("_id").apply(request)

      contentAsJson(result).mustEqual(sanitize(element))
      status(result).mustEqual(201)
    }
    "fail to create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(credentialManager.createFromPassword(any[String])).thenReturn(Future.failed(new Exception("Invalid credential format")))
      val result = credentialController.create("_id").apply(request)

      status(result).mustEqual(500)
    }
    "fail to create with wrong body" in {
      val request = FakeRequest(POST, path).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val result = credentialController.create().apply(request)

      status(result).mustEqual(400)
    }
    "delete by user id" in {
      val request = FakeRequest(POST, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(credentialDBDAO.deleteById("password")).thenReturn(Future.successful((): Unit))
      val result = credentialController.deleteByUserId("_id").apply(request)

      status(result).mustEqual(204)
    }
  }

}
