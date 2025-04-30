package managers

import applications.controllers.ApplicationsManager
import applications.dao.ApplicationDao
import applications.model.body.ApplicationInput
import applications.model.dao.Application
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import play.api.Configuration
import play.api.test.PlaySpecification
import users.dao.UserDao
import users.model.dao.User
import wallets.dao.WalletDao
import wallets.model.dao.Wallet
import mockws.MockWSHelpers.materializer
import org.joda.time.DateTime
import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.*
import org.scalatestplus.play.*
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

class ApplicationsManagerTest extends PlaySpecification {

  sequential
  val configuration: Configuration = mock(classOf[Configuration])
  val machineDAOMock: ApplicationDao = mock(classOf[ApplicationDao])
  val userDaoMock: UserDao = mock(classOf[UserDao])
  val walletDao: WalletDao = mock(classOf[WalletDao])
  val machineManager = new ApplicationsManager(configuration, walletDao, machineDAOMock, userDaoMock)
  val machine: Application = Application(Some("id1"), "machine1", Some("192.168.1.1"), "generatedApiKey", "$2a$10$GSbeCBYFaT88pLHq6OEbDe", "ethereum", "walletId")
  val machineInput: ApplicationInput = ApplicationInput("machine1", Some("192.168.1.1"), "0x0000", "ethereum")
  val newMachine: Application = Application(Some("id1"), "machine2", Some("192.168.1.2"), "generatedApiKey", "$2a$10$GSbeCBYFaT88pLHq6OEbDe", "ethereum", "walletId")
  val user = new User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("id"), List("admin"), "compagnyId")

  "MachineManagerTest" should {
    "createMachine" in {
      val wallet: Wallet = Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "_id")
      when(walletDao.findOneByAddress(any[String], any[String], any[String])).thenReturn(Future.successful(wallet))
      when(machineDAOMock.create(any[Application])).thenReturn(Future.successful(machine))
      when(userDaoMock.findById("_id")).thenReturn(Future.successful(user))
      when(userDaoMock.updateById("_id", user)).thenReturn(Future.successful(User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("id", "id1"), List("admin"), "compagnyId")))

      val result = Await.result(machineManager.createMachine("_id", "ethereum", machineInput), Duration.Inf)
      result._1.length mustEqual 40
      result._2.length mustEqual 36
    }
  }
}
