package managers

import companies.controllers.CompaniesManager
import companies.dao.CompanyDao
import companies.model.body.CompanyInput
import companies.model.dao.Company
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.PlaySpecification
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CompaniesManagerTest extends PlaySpecification {
  sequential
  val company: Company = Company(Some("_idCompany"), List("siret1"))
  val companyInput: CompanyInput = CompanyInput("siret1")
  val user: User = User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  val userDAO: UserDao = mock(classOf[UserDao])
  val companyDAO: CompanyDao = mock(classOf[CompanyDao])
  val companyManager: CompaniesManager = new CompaniesManager(companyDAO, userDAO)

  "CompanyManagerTest" should {

    "createCompany" in {
      when(companyDAO.create(any[Company])).thenReturn(Future.successful(company))
      val result = Await.result(companyManager.createCompany(companyInput), Duration.Inf)
      result._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true
    }
  }
}
