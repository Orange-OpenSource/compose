package dao

import common.dao.AbstractDao
import org.mockito.Mockito.when
import wallets.dao.WalletDao
import wallets.model.dao.Wallet

import scala.concurrent.{Await, Future}
import scala.concurrent.duration.Duration

class WalletDaoTest extends AbstractDaoTest[Wallet] {

  override val collectionName: String = "wallets"
  override val element: Wallet = Wallet(Some("_id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
  override val updatedElement: Wallet = Wallet(Some("_id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet2", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
  override val secondElement: Wallet = Wallet(Some("_id2"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "alastria", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
  override val dao: AbstractDao[Wallet] = new WalletDao()
  val walletDao: WalletDao = new WalletDao()
  /*"WalletDaoTest" should {

    "find one by address" in {
      val mockDao = mock[AbstractDao[Wallet]]
      Await.ready(dao.create(element), Duration.Inf)
      when(mockDao.findBy("userId")("_id")).thenReturn(Future.successful(List(element)))
      val i = Await.result(walletDao.findOneByAddress("_id","ethereum","c1499b34eace8c4a29becab8a9d05b156781f958"), Duration.Inf)
      i mustEqual element
    }
  }*/
}
