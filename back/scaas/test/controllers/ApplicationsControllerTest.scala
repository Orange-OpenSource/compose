package controllers

import applications.controllers.{ApplicationsController, ApplicationsManager}
import applications.dao.ApplicationDao
import applications.model.body.ApplicationInput
import applications.model.dao.Application
import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import common.exceptions.DAOException
import mockws.MockWSHelpers.materializer
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat, __}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, PUT}
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.Future

class ApplicationsControllerTest extends AbstractIdentityControllerTest[Application] {
  override val sanitize: Application => JsValue = JsonSanitizer(__ \ "hashedApiKey", __ \ "salt")("applicationId")(Application.format)
  override val element: Application = Application(Some("_id"), "machine1", Some("0.0.0.0"), "apiKey", "salt", "ethereum", "walletId")
  override val elementId: String = "_id2"
  override val path: String = "/api/v3/machines/"
  override val newElement: Application = Application(Some("_id2"), "machine2", Some("0.0.0.1"), "apiKey2", "salt", "ethereum", "walletId")
  val applicationInput: ApplicationInput = ApplicationInput("machine1", Some("0.0.0.1"), "0x000000", "ethereum")
  override val body: JsValue = Json.parse("""{"name":"machine1","ipAddr":"0.0.0.1","walletAddress":"0x000000"}""")
  val applicationDao: ApplicationDao = mock(classOf[ApplicationDao])
  val userDao: UserDao = mock(classOf[UserDao])

  val applicationManager: ApplicationsManager = mock(classOf[ApplicationsManager])
  override val abstractUserController: AbstractIdentityController[Application] = new ApplicationsController(applicationDao, userDao, applicationManager, tokenDao, executionContext, cc)
  override val dao: AbstractDao[Application] = applicationDao


  val machineController = new ApplicationsController(applicationDao, userDao, applicationManager, tokenDao, executionContext, cc)
  override implicit val format: OFormat[Application] = Json.format[Application]
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_id"), List("ADMIN", "USER"), "compagnyId")

  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("ADMIN", "USER"), "compagnyId")))

  "Application Controller" should {
    "create" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/applications").withBody(Json.toJson(applicationInput)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(applicationManager.createMachine(clientId, "ethereum", applicationInput)).thenReturn(Future.successful("apiKey", "applicationId"))
      val result = machineController.create(clientId).apply(request)

      status(result) mustEqual 201
    }
    "fail to create" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/applications").withBody(Json.toJson(applicationInput)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(applicationManager.createMachine(clientId, "ethereum", applicationInput)).thenReturn(Future.failed(DAOException("")))
      val result = machineController.create(clientId).apply(request)

      status(result) mustEqual 500
    }
    "fail to create with wrong body" in {
      val request = FakeRequest(POST, path).withBody(Json.toJson("")).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val result = machineController.create().apply(request)

      status(result) mustEqual 400
    }

    "fail to update with wrong body" in {
      val request = FakeRequest(PUT, path + elementId).withBody(Json.toJson("")).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val result = machineController.update(elementId).apply(request)

      status(result) mustEqual 400
    }
    "delete" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(userDao.updateById("_id", user)).thenReturn(Future.successful(user))
      when(applicationDao.deleteById("id")).thenReturn(Future.successful((): Unit))
      val result = machineController.delete("id").apply(request)

      status(result) mustEqual 204
    }
    "delete Application" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(userDao.updateById("_id", user)).thenReturn(Future.successful(user))
      when(applicationDao.deleteById("id")).thenReturn(Future.successful((): Unit))
      val result = machineController.delete("_id", "id").apply(request)

      status(result) mustEqual 204
    }
    "get all user Application" in {
      val request = FakeRequest(GET, "/api/v3/users/_id/applications").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))

      when(applicationDao.findOneBy("_id")("_id")).thenReturn(Future.successful(element))
      val result = machineController.getAllUserApplications("_id").apply(request)

      status(result) mustEqual 200
    }
    "fail to get all user Application" in {
      val request = FakeRequest(GET, "/api/v3/users/_id/applications").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("id")).thenReturn(Future.failed(new Exception("failed")))

      when(applicationDao.findOneBy("_id")("id")).thenReturn(Future.failed(new Exception("failed")))
      val result = machineController.getAllUserApplications("id").apply(request)

      status(result) mustEqual 500
    }

    "get getApplicationById" in {
      val request = FakeRequest(GET, "/api/v3/users/_id/applications").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))

      when(applicationDao.findById("_id")).thenReturn(Future.successful(element))
      val result = machineController.getApplicationById("_id", "_id").apply(request)

      status(result) mustEqual 200
    }
    "unauthorized to getApplicationById" in {
      val request = FakeRequest(GET, "/api/v3/users/_id/applications").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(applicationDao.findById("_id")).thenReturn(Future.successful(element))
      val result = machineController.getApplicationById("_id", "id").apply(request)

      status(result) mustEqual 401
    }
    "failed to getApplicationById" in {
      val request = FakeRequest(GET, "/api/v3/users/_id/applications").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(applicationDao.findById("_id")).thenReturn(Future.failed(new Exception()))
      val result = machineController.getApplicationById("_id", "_id").apply(request)

      status(result) mustEqual 500
    }
  }

}
