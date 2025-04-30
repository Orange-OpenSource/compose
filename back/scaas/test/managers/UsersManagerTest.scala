package managers

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.exceptions.{ConflictException, DAOException}
import credentials.controllers.CredentialsManager
import credentials.model.dao.Credential
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.PlaySpecification
import users.controllers.UsersManager
import users.dao.UserDao
import users.model.body.{UserInput, UserInputUpdate}
import users.model.dao.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class UsersManagerTest extends PlaySpecification {
  sequential
  val machine: Application = Application(Some("_idMachine"), "machine1", Some("0.0.0.0"), "apiKey", "salt", "ethereum", "walletId")
  val user: User = User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_idMachine"), List("admin"), "compagnyId")
  val userUpdated: User = User(Some("_id"), "name", "familyname2", "test@test.com", "0260566058", "password", List("machine1"), List("USER"), "compagnyId")
  val userInput: UserInput = UserInput("name", "familyname", "0260566058", "test@test.com", "password", List("USER"), "companyId")
  val userInputUpdate: UserInputUpdate = UserInputUpdate(Some("name"), Some("familyname2"), Some("0260566058"), Some(List("USER")))
  val userfailed: User = User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  val userfailedInput: UserInput = UserInput("name", "familyname", "0260566058", "test@test.com", "password", List("USER"), "companyId")
  val credential: Credential = Credential(Some("id1"), "salt", "Password1!", Some("otp"))
  val userDBDAO: UserDao = mock(classOf[UserDao])
  val machineDBDAO: ApplicationDao = mock(classOf[ApplicationDao])
  val credentialManager: CredentialsManager = mock(classOf[CredentialsManager])
  val userManager = new UsersManager(userDBDAO, machineDBDAO, credentialManager)
  "UserManagerTest" should {
    "getMachines" in {
      when(userDBDAO.findById("_id")).thenReturn(Future.successful(user))
      when(machineDBDAO.findById("_idMachine")).thenReturn(Future.successful(machine))
      val result = Await.result(userManager.getMachines("_id"), Duration.Inf)
      result.head.name mustEqual machine.name
      result.head.hashedApiKey mustEqual machine.hashedApiKey

    }

    "createUser" in {
      when(credentialManager.createFromPassword(any[String])).thenReturn(Future.successful(credential))
      when(userDBDAO.create(any[User])).thenReturn(Future.successful(user))
      when(userDBDAO.findBy("email")("test@test.com")).thenReturn(Future.successful(List()))

      val result = Await.result(userManager.createUser(userInput), Duration.Inf)
      result._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true

    }
    "fail to createUser" in {
      when(credentialManager.createFromPassword(any[String])).thenReturn(Future.successful(credential))
      when(userDBDAO.create(any[User])).thenReturn(Future.successful(user))
      when(userDBDAO.findBy("email")("test@test.com")).thenReturn(Future.failed(DAOException("email already exist")))

      Await.result(userManager.createUser(userInput), Duration.Inf) must throwA[Exception]("email already exist")

    }
    "fail to createUser user already exist" in {
      when(credentialManager.createFromPassword(any[String])).thenReturn(Future.successful(credential))
      when(userDBDAO.create(any[User])).thenReturn(Future.successful(user))
      when(userDBDAO.findBy("email")("test@test.com")).thenReturn(Future.successful(List(User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("_idMachine"), List("admin"), "compagnyId"))))

      Await.result(userManager.createUser(userInput), Duration.Inf) must throwA(ConflictException("email already exist"))

    }

    "updateUser" in {
      when(userDBDAO.findById("_id")).thenReturn(Future.successful(user))
      when(userDBDAO.updateById(any[String], any[User], any[Boolean])).thenReturn(Future.successful(userUpdated))
      val result = Await.result(userManager.updateUser(userInputUpdate, "_id"), Duration.Inf)

      result.familyName mustEqual userUpdated.familyName
      result.telephone mustEqual userUpdated.telephone
      result.roles mustEqual userUpdated.roles

    }
  }
}
