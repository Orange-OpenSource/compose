package controllers

import common.controllers.{AbstractIdentityController, JsonSanitizer}
import common.dao.AbstractDao
import common.exceptions.DAOException
import companies.controllers.{CompaniesManager, CompanyController}
import companies.dao.CompanyDao
import companies.model.body.CompanyInput
import companies.model.dao.Company
import mockws.MockWSHelpers.materializer
import org.mockito.Mockito.{mock, when}
import play.api.libs.json.{JsValue, Json, OFormat}
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, POST, defaultAwaitTimeout, status}
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.Future


class CompanyControllerTest extends AbstractIdentityControllerTest[Company] {
  override val sanitize: Company => JsValue = JsonSanitizer()("companyId")(Company.format)
  override val element: Company = new Company(Some("_id1"), List("siret1"))
  val elementInput: CompanyInput = new CompanyInput("siret1")
  override val newElement: Company = new Company(Some("_id2"), List("siret2"))
  val newElementInput: CompanyInput = new CompanyInput("siret2")
  override val elementId: String = "_id2"
  override val body: JsValue = Json.parse("""{"siret":"siret1"}""")
  val companyDBDAO: CompanyDao = mock(classOf[CompanyDao])
  val userDao: UserDao = mock(classOf[UserDao])
  val companyManager: CompaniesManager = mock(classOf[CompaniesManager])
  override val abstractUserController: AbstractIdentityController[Company] = new CompanyController(companyDBDAO, userDao, tokenDao, companyManager, executionContext, cc)
  override val path: String = "/api/v3/companies/"
  override val dao: AbstractDao[Company] = companyDBDAO
  val companyController: CompanyController = new CompanyController(companyDBDAO, userDao, tokenDao, companyManager, executionContext, cc)
  override val format: OFormat[Company] = Json.format[Company]

  when(tokenDao
    .findOneBy("userToken")(token.userToken))
    .thenReturn(Future.successful(token))
  when(userDao.findById(clientId)).thenReturn(Future.successful(User(Some(clientId), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("SUPER", "ADMIN", "USER"), "compagnyId")))

  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_id"), List("SUPER", "ADMIN", "USER"), "compagnyId")
  "company controller" should {
    "create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(companyManager.createCompany(elementInput)).thenReturn(Future.successful(element))
      val result = companyController.create().apply(request)

      status(result) mustEqual 201
    }
    "fail to create" in {
      val request = FakeRequest(POST, path).withBody(body).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      when(companyManager.createCompany(elementInput)).thenReturn(Future.failed(DAOException("")))
      val result = companyController.create().apply(request)
      status(result) mustEqual 500
    }
    "fail to create with wrong body" in {
      val request = FakeRequest(POST, path).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findById("_id")).thenReturn(Future.successful(user))
      val result = companyController.create().apply(request)
      status(result) mustEqual 400
    }

    "get users" in {
      val request = FakeRequest(GET, path).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findBy("companyId")("_id")).thenReturn(Future.successful(Seq(user)))
      val result = companyController.getUsers("_id").apply(request)
      status(result) mustEqual 200
    }
    "fail to get users" in {
      val request = FakeRequest(POST, path).withBody(Json.toJson("")).withHeaders("user-id" -> clientId, "user-token" -> token.userToken)
      when(userDao.findBy("companyId")("_id")).thenReturn(Future.failed(new Exception()))
      val result = companyController.getUsers("_id").apply(request)
      status(result) mustEqual 500
    }
  }
}

