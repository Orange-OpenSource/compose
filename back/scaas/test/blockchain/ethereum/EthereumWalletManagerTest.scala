package blockchain.ethereum

import org.web3j.crypto.{ECKeyPair, Keys}
import play.api.Configuration
import org.mockito.Mockito.*
import org.mockito.ArgumentMatchers._
import play.api.test.PlaySpecification
import wallets.controllers.ethereum.EthereumWalletManager
import wallets.model.body.ConfigWallet
import wallets.model.dao.Wallet
import wallets.utils.EthereumWalletUtils

class EthereumWalletManagerTest extends PlaySpecification{

  val conf: Configuration = mock(classOf[Configuration])

  when(conf.get[String]("blockchain.wallet.master-key")).thenReturn("toto")

  "EthereumWalletManagerMock" should {

    "generate a new wallet" in {
      val oWalletUtils = mock(classOf[EthereumWalletUtils])
      val configWallet = ConfigWallet("name", "password", "ehtereum")
      val walletModel = new Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
      when(oWalletUtils.createWallet(any[String], any[ECKeyPair])).thenReturn(EthereumWalletManager().loadWallet(walletModel.file))

      val wallet = new EthereumWalletManager(oWalletUtils).generateWallet("name", configWallet.password, "ethereum", "")(conf)

      wallet.file mustEqual walletModel.file
      wallet._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true
      wallet.blockchain mustEqual "ethereum"

    }
    "import wallet" in {
      val oWalletUtils = mock(classOf[EthereumWalletUtils])
      val configWallet = ConfigWallet("name", "password", "ehtereum")
      val walletModel = new Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
      when(oWalletUtils.createWallet(any[String], any[ECKeyPair])).thenReturn(EthereumWalletManager().loadWallet(walletModel.file))
      val ecKeyPair = Keys.createEcKeyPair()
      val keyPair = smart_contracts.model.blockchain.KeyPair(ecKeyPair.getPrivateKey, ecKeyPair.getPublicKey)
      val wallet = new EthereumWalletManager(oWalletUtils).importWallet("name", configWallet.password, keyPair, "ethereum", "")(conf)

      wallet.file mustEqual walletModel.file
      wallet._id.get.matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}") mustEqual true
      wallet.blockchain mustEqual "ethereum"

    }
    "load a wallet" in {
      val walletModel = new Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "userid")
      EthereumWalletManager().loadWallet(walletModel.file).getAddress mustEqual "c1499b34eace8c4a29becab8a9d05b156781f958"
    }
  }
}
