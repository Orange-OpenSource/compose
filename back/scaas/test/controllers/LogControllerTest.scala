package controllers

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, defaultAwaitTimeout, status}
import smart_contracts.controllers.LogController
import smart_contracts.dao.LogDao
import smart_contracts.model.dao.LogProject
import specs2.arguments.sequential
import users.model.dao.User

import scala.concurrent.Future

class LogControllerTest extends AbstractIdentityControllerTest[LogProject] {

  sequential

  override val sanitize: LogProject => JsValue = JsonSanitizer()("_id")(LogProject.format)
  override val element: LogProject = new LogProject("_id", "project_1", "method_1", "1720006719", "short_log")
  override val newElement: LogProject = new LogProject("_id2", "project_2", "method_2", "1720006758", "very_long_log")
  override val elementId: String = "_id"
  override val body: JsValue = null
  val logDao: LogDao = mock(classOf[LogDao])
  override val abstractUserController: AbstractIdentityController[LogProject] = new LogController(logDao, userDBDAO, tokenDao, executionContext, cc)
  override val path: String = "/api/v3/log/"
  override val dao: AbstractDao[LogProject] = logDao
  val logController: LogController = new LogController(logDao, userDBDAO, tokenDao, executionContext, cc)
  override val format: OFormat[LogProject] = Json.format[LogProject]


  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDBDAO.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))

  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_id"), List("SUPER", "ADMIN", "USER"), "compagnyId")


  "LogProjectControllerTest" should {
    "get log by project id" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("projectId")("project_1")).thenReturn(Future.successful(List(element)))

      val result = logController.getLogByProjectId("_id", "project_1").apply(request)

      status(result) mustEqual 200

    }
    "fails to get log by project id" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("projectId")("project_1")).thenReturn(Future.failed(new Exception()))

      val result = logController.getLogByProjectId("_id", "project_1").apply(request)

      status(result) mustEqual 500

    }

    "get log by project id and method" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("projectId")("project_1")).thenReturn(Future.successful(List(element)))

      val result = logController.getLogByProjectIdAndMethod("_id", "project_1", "method_1").apply(request)

      status(result) mustEqual 200

    }
    "fails to get log by project id and method" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("projectId")("project_1")).thenReturn(Future.failed(new Exception()))

      val result = logController.getLogByProjectIdAndMethod("_id", "project_1", "method_1").apply(request)

      status(result) mustEqual 500

    }

    "get log by timestamp" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("timestamp")("1720006719")).thenReturn(Future.successful(List(element)))

      val result = logController.getLogByTimestamp("_id", "1720006719").apply(request)

      status(result) mustEqual 200

    }
    "fails to get log by timestamp" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findBy("timestamp")("1720006719")).thenReturn(Future.failed(new Exception()))

      val result = logController.getLogByTimestamp("_id", "1720006719").apply(request)

      status(result) mustEqual 500

    }

    "get log between two dates" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findAll()).thenReturn(Future.successful(List(element, newElement)))

      val result = logController.getLogByDate("_id", "1720006719", "1720006758").apply(request)

      status(result) mustEqual 200

    }
    "fails to get log between two date" in {

      val request = FakeRequest(GET, path).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)

      when(logDao.findAll()).thenReturn(Future.failed(new Exception()))

      val result = logController.getLogByDate("_id", "1720006719", "1720006758").apply(request)

      status(result) mustEqual 500

    }

  }

}
