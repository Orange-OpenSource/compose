package controllers

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import common.exceptions.DAOException
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.test.FakeRequest
import users.controllers.{UserController, UsersManager}
import users.model.body.{UserInput, UserInputUpdate}
import users.model.dao.User

import scala.concurrent.Future

class UserControllerTest extends AbstractIdentityControllerTest[User] {
  override val sanitize: User => JsValue = JsonSanitizer()("userId")(User.format)
  override val element: User = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")
  val elementInput: UserInput = new UserInput("name", "familyname", "0260566058", "test@test.com", "password", List("SUPER", "ADMIN", "USER"), "companyId")
  override val newElement: User = new User(Some("_id2"), "name", "familyname", "test2@test.com", "0600000000", "password", List("machine2"), List("SUPER", "ADMIN", "USER"), "companyId")
  val newElementInput: UserInput = new UserInput("name", "familyname", "0260566058", "test2@test.com", "password", List("SUPER", "ADMIN", "USER"), "companyId")
  override val elementId: String = "_id2"
  override val body: JsValue = Json.parse("""{"name":"name","familyName":"familyName","telephone":"0600000000","email":"test2@test.com","password":"password","roles": ["SUPER","ADMIN","USER"],"companyId":"companyid"}""")
  val partialBody: JsValue = Json.parse("""{"telephone":"0600000000"}""")
  val userManager: UsersManager = mock(classOf[UsersManager])
  override val dao: AbstractDao[User] = userDBDAO
  override val abstractUserController: AbstractIdentityController[User] = new UserController(userDBDAO, tokenDao, userManager, executionContext, cc)
  override val path: String = "/api/v3/users/"
  val machineDBDAO: ApplicationDao = mock(classOf[ApplicationDao])
  val userController = new UserController(userDBDAO, tokenDao, userManager, executionContext, cc)
  override implicit val format: OFormat[User] = Json.format[User]
  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDBDAO.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))
  "UserControllerTest" should {
    "getMachines" in {
      val machine = new Application(Some("_id"), "machine1", Some("12.12.12.12"), "apikey", "salt", "ethereum", "walletId")
      val request = FakeRequest(GET, "/api/v3/users/id/machines").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(machineDBDAO.findById("_id")).thenReturn(Future.successful(machine))
      when(userManager.getMachines("_id")).thenReturn(Future.successful(List(new Application(Some("_id"), "machine1", Some("12.12.12.12"), "apikey", "salt", "ethereum", "walletId"))))
      val result = userController.getMachines("_id").apply(request)
      status(result) mustEqual 200
    }
    "fail to getMachines" in {
      val machine = new Application(Some("_id"), "machine1", Some("12.12.12.12"), "apikey", "salt", "ethereum", "walletId")
      val request = FakeRequest(GET, "/api/v3/users/id/machines").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(machineDBDAO.findById("_id")).thenReturn(Future.successful(machine))
      when(userDBDAO.findById("_id")).thenReturn(Future.successful(element))
      when(userManager.getMachines("_id")).thenReturn(Future.failed(DAOException("")))
      val result = userController.getMachines("_id").apply(request)
      status(result) mustEqual 500

    }

    "create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userManager.createUser(any[UserInput])).thenReturn(Future.successful(element))
      val result = userController.create().apply(request)

      status(result) mustEqual 201
    }
    "fail to create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userManager.createUser(any[UserInput])).thenReturn(Future.failed(DAOException("")))
      val result = userController.create().apply(request)
      status(result) mustEqual 500
    }
    "fail to create with wrong body" in {
      val request = FakeRequest(POST, path).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val result = userController.create().apply(request)
      status(result) mustEqual 400
    }

    "update" in {
      val request = FakeRequest(PUT, path).withBody(partialBody).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userManager.updateUser(any[UserInputUpdate], any[String])).thenReturn(Future.successful(element))
      val result = userController.update("_id").apply(request)
      status(result) mustEqual 200
    }
  }

}
