package blockchain.ethereum.mocks

import org.web3j.crypto.WalletFile
import play.api.Configuration
import smart_contracts.model.blockchain.KeyPair
import wallets.controllers.WalletManager
import wallets.model.dao.Wallet

class EthereumWalletManagerMock() extends WalletManager {
  override def generateWallet(name: String, userPassword: String, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet = {
    Wallet(Some("id"), "c1499b34eace8c4a29becab8a9d05b156781f958", "wallet1", "ethereum", "salt", "{\"address\":\"c1499b34eace8c4a29becab8a9d05b156781f958\",\"id\":\"17e7e824-81ab-4947-9569-b700e0217d69\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"ciphertext\":\"4b4ca3563bac839ef592ea1e5effe62c18c9437eefdc3161e6aedbcaef8e2f39\",\"cipherparams\":{\"iv\":\"f0e2ae8323d09cf9621283b05f1491d9\"},\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":262144,\"p\":1,\"r\":8,\"salt\":\"218d706616c5adca15a0fe2ef24f5f5e5e5c0bcbcc0b6e11ea5edfdb595531b2\"},\"mac\":\"9f87f4201b7f1d9fb894dda75bafd8d12bb6cad1ad55e0b226c2ca21dfdb519e\"}}", "clientId")
  }

  override def loadWallet(walletContent: String): WalletFile = ???

  override def importWallet(name: String, userPassword: String, keyPair: KeyPair, blockchain: String, userId: String)(implicit configuration: Configuration): Wallet = {
    ???
  }

  override def getKeyPair(walletContent: String, walletPassword: String): KeyPair = ???
}
