package controllers

import common.exceptions.DAOException
import play.api.libs.json.Format.GenericFormat
import play.api.libs.json.{JsValue, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsJson, defaultAwaitTimeout, status}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.specs2.matcher.MustMatchers.theValue

import scala.concurrent.Future

abstract class AbstractControllerCommonTest[T] extends AbstractIdentityControllerTest[T]  {


  "AbstractControllerCommonTest" should  {
    "create" in  {
      val request = FakeRequest(POST, path).withBody(body)
      when(dao.create(any[T]())).thenReturn(Future.successful(newElement))
      val result = abstractUserController.create().apply(request)

      (contentAsJson(result) \ "_id").get.mustNotEqual("")
      status(result) mustEqual 201
    }
    "failed to create with wrong body" in  {
      val request = FakeRequest(POST, path).withBody(Json.toJson(""))
      val result = abstractUserController.create().apply(request)
      status(result) mustEqual 400
    }
    "fail to create" in {
      val request = FakeRequest(POST, path).withBody(body)
      when(dao.create(any[T]())).thenReturn(Future.failed(DAOException("")))
      val result = abstractUserController.create().apply(request)
      (contentAsJson(result) \ "message").get mustEqual Json.toJson("Parsing error")
      status(result) mustEqual 500
    }
    "update" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body)
      when(dao.updateById(elementId, newElement)).thenReturn(Future.successful(newElement))
      val result = abstractUserController.update(elementId).apply(request)
      contentAsJson(result) mustEqual Json.toJson(newElement)
      status(result) mustEqual 200
    }
    "fail to update with wrong body" in {
      val request = FakeRequest(PUT, path + elementId).withBody(Json.toJson(""))
      val result = abstractUserController.update(elementId).apply(request)
      status(result) mustEqual 400
    }
    "fail to update" in {
      val request = FakeRequest(PUT, path + elementId).withBody(body)
      when(dao.updateById(elementId, newElement)).thenReturn(Future.failed(DAOException("")))
      val result = abstractUserController.update(elementId).apply(request)
      (contentAsJson(result) \ "message").get mustEqual Json.toJson("Parsing error")
      status(result) mustEqual 500
    }


  }
}
