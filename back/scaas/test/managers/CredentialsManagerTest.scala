package managers

import common.exceptions.DAOException
import credentials.controllers.CredentialsManager
import credentials.dao.CredentialDao
import credentials.model.body.CredentialInput
import credentials.model.dao.Credential
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.test.PlaySpecification
import users.dao.UserDao
import users.model.dao.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class CredentialsManagerTest extends PlaySpecification {

  sequential
  val credentialDAOMock: CredentialDao = mock(classOf[CredentialDao])
  val userDAOMock: UserDao = mock(classOf[UserDao])
  val credentialManager = new CredentialsManager(credentialDAOMock, userDAOMock)
  val credential: Credential = Credential(Some("id1"), "$2a$10$GSbeCBYFaT88pLHq6OEbDe", "Password1!", Some("otp"))
  val credentialInput: CredentialInput = CredentialInput("Password12!", Some("otp"))
  val newCredential: Credential = Credential(Some("id1"), "$2a$10$GSbeCBYFaT88pLHq6OEbDe", "Password12!", Some("otp"))
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "id1", List("machine1"), List("admin"), "compagnyId")
  "CredentialManagerTest" should {
    "createFromPassword" in {
      when(credentialDAOMock.create(any[Credential])).thenReturn(Future.successful(credential))
      val result = Await.result(credentialManager.createFromPassword("Password1!"), Duration.Inf)
      result._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true
      result.hashedPassword.length mustEqual 60
    }
    "fail to create password" in {
      Await.result(credentialManager.createFromPassword("test"), Duration.Inf) must throwA[Exception]
    }
    "fail to create password with invalid format" in {
      Await.result(credentialManager.createFromPassword("invalid"), Duration.Inf) must throwA[Exception]("Invalid credential format")
    }

    "updatePassword" in {
      when(userDAOMock.findById("_id")).thenReturn(Future.successful(user))
      when(credentialDAOMock.findById("id1")).thenReturn(Future.successful(credential))
      when(credentialDAOMock.updateById(any[String], any[Credential], any[Boolean])).thenReturn(Future.successful(newCredential))
      val result = Await.result(credentialManager.updateCredential("_id", credentialInput), Duration.Inf)
      result mustEqual newCredential

    }
    "fail to updatePassword" in {
      Await.result(credentialManager.updateCredential("id", credentialInput), Duration.Inf) must throwA[Exception]
    }
    "fail to updatePassword when credential not found" in {
      when(userDAOMock.findById("nonExistentId")).thenReturn(Future.failed(DAOException("Credential with ID nonExistentId not found")))
      when(credentialDAOMock.findById("id1")).thenReturn(Future.successful(credential))
      when(credentialDAOMock.updateById(any[String], any[Credential], any[Boolean])).thenReturn(Future.successful(newCredential))
      // when(credentialDAOMock.findById("nonExistentId")).thenReturn(Future.failed(DAOException("Credential with ID nonExistentId not found")))
      Await.result(credentialManager.updateCredential("nonExistentId", credentialInput), Duration.Inf) must throwA[Exception]("Credential with ID nonExistentId not found")
    }
  }
}
