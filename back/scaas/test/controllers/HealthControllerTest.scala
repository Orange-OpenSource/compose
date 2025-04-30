package controllers

import common.controllers.HealthController
import companies.controllers.CompaniesManager
import companies.model.body.CompanyInput
import companies.model.dao.Company
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.mvc.{ControllerComponents, Results}
import play.api.test.{FakeRequest, PlaySpecification}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.MongoConnection
import smart_contracts.dao.{ContractDBDAO, LogDao}
import users.controllers.UsersManager
import users.dao.UserDao

import scala.concurrent.{ExecutionContext, Future}

class HealthControllerTest extends PlaySpecification with Results {
  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  implicit lazy val cc: ControllerComponents = injector.instanceOf[ControllerComponents]
  val companyManager: CompaniesManager = mock(classOf[CompaniesManager])
  val userDao: UserDao = mock(classOf[UserDao])
  val userManager : UsersManager = mock(classOf[UsersManager])
  val contractDao : ContractDBDAO = mock(classOf[ContractDBDAO])
  val logDao : LogDao = mock(classOf[LogDao])
  val reactiveMongoApi : ReactiveMongoApi = mock(classOf[ReactiveMongoApi])
  val configuration: Configuration = mock(classOf[Configuration])
  val healthController : HealthController = new HealthController(cc,companyManager,userDao, userManager, contractDao, logDao, reactiveMongoApi , configuration)

  "HealthController" should {
    "status" in {

      val request = FakeRequest(GET, "/api/v3/status")
      when(userDao.count()).thenReturn(Future.successful(0L))
      val mongConnection : MongoConnection = mock(classOf[MongoConnection])
      when(companyManager.createCompany(any[CompanyInput])).thenReturn(Future.successful(Company(Some("_id"),List("siret"))))
      when(reactiveMongoApi.connection).thenReturn(mongConnection)
      when(mongConnection.active).thenReturn(true)
      val result = healthController.status.apply(request)
      status(result).mustEqual(200)
    }

  }
}
