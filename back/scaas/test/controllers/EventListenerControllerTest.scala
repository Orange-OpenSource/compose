package controllers

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import mockws.MockWSHelpers.materializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, defaultAwaitTimeout, status}
import smart_contracts.controllers.EventListenerController
import smart_contracts.dao.WebhookListenersDao
import smart_contracts.model.dao.WebhookListener
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.Future

class EventListenerControllerTest extends AbstractIdentityControllerTest[WebhookListener] {
  override val element: WebhookListener = WebhookListener("_id", "userId", "ethereum", "0x0000", "event", "127.0.0.1")
  override val newElement: WebhookListener = WebhookListener("_id2", "userId", "ethereum", "0x0000", "event", "127.0.0.1")
  override val elementId: String = "_id"
  val webHookListenersDao: WebhookListenersDao = mock(classOf[WebhookListenersDao])
  override val dao: AbstractDao[WebhookListener] = webHookListenersDao
  override val body: JsValue = Json.parse("""{"url": "127.0.0.1", "blockchain": "ethereum"}""")
  override implicit val format: OFormat[WebhookListener] = Json.format[WebhookListener]
  override val abstractUserController: AbstractIdentityController[WebhookListener] = new EventListenerController(webHookListenersDao, userDBDAO, tokenDao, executionContext, cc)
  override val path: String = "/api/v3/webhooks"
  override val sanitize: WebhookListener => JsValue = JsonSanitizer()("webhookId")(WebhookListener.format)
  val userDao: UserDao = mock(classOf[UserDao])
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_id"), List("ADMIN", "USER"), "compagnyId")
  val eventListenerController: EventListenerController = new EventListenerController(webHookListenersDao, userDBDAO, tokenDao, executionContext, cc)
  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("ADMIN", "USER"), "compagnyId")))

  "event listener Controller" should {
    "create webhook" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/webhook").withBody(Json.toJson(body)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(webHookListenersDao.create(any[WebhookListener])).thenReturn(Future.successful(element))
      val result = eventListenerController.createWebhook(clientId, "0x0000", "event").apply(request)

      status(result) mustEqual 201
    }
    "fails create webhook" in {
      val request = FakeRequest(POST, "/api/v3/users/" + clientId + "/webhook").withBody(Json.toJson(body)).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(webHookListenersDao.create(any[WebhookListener])).thenReturn(Future.failed(new Exception()))
      val result = eventListenerController.createWebhook(clientId, "0x0000", "event").apply(request)

      status(result) mustEqual 500
    }
    "get webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val webhook = WebhookListener("_id", clientId, "ethereum", "0x0000", "event", "127.0.0.1")
      when(webHookListenersDao.findById("_id")).thenReturn(Future.successful(webhook))
      val result = eventListenerController.getWebhook("ethereum", "0x0000", "event", "_id").apply(request)

      status(result) mustEqual 200
    }
    "fails to get webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(webHookListenersDao.findById("_id")).thenReturn(Future.failed(new Exception()))
      val result = eventListenerController.getWebhook("ethereum", "0x0000", "event", "_id").apply(request)

      status(result) mustEqual 500
    }
    "get all webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val webhook = WebhookListener("_id", clientId, "ethereum", "0x0000", "event", "127.0.0.1")
      when(webHookListenersDao.findBy("userId")(clientId)).thenReturn(Future.successful(List(webhook)))
      val result = eventListenerController.getAllWebhook(clientId, "0x0000", "event").apply(request)

      status(result) mustEqual 200
    }
    "fails to get all webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(webHookListenersDao.findBy("userId")(clientId)).thenReturn(Future.failed(new Exception()))
      val result = eventListenerController.getAllWebhook(clientId, "0x0000", "event").apply(request)

      status(result) mustEqual 500
    }

    "delete webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      val webhook = WebhookListener("_id", clientId, "ethereum", "0x0000", "event", "127.0.0.1")
      when(webHookListenersDao.findById("_id")).thenReturn(Future.successful(webhook))
      when(webHookListenersDao.deleteById("_id")).thenReturn(Future.successful((): Unit))
      val result = eventListenerController.deleteWebHook("ethereum", "0x0000", "event", "_id").apply(request)

      status(result) mustEqual 204
    }
    "fails to delete webhook" in {
      val request = FakeRequest(GET, "/api/v3/users/" + clientId + "/webhook").withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(webHookListenersDao.findBy("userId")(clientId)).thenReturn(Future.failed(new Exception()))
      when(webHookListenersDao.deleteById("_id")).thenReturn(Future.failed(new Exception()))
      val result = eventListenerController.deleteWebHook("ethereum", "0x0000", "event", "_id").apply(request)

      status(result) mustEqual 500
    }
  }
}
