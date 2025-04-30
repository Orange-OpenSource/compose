package utils

import org.web3j.crypto.Keys
import play.api.test.PlaySpecification
import wallets.utils.EthereumWalletUtils

class EthereumWalletUtilsTest extends PlaySpecification {

  "EthereumWalletUtilsTest" should {
    "createWallet of version 3" in {
      EthereumWalletUtils().createWallet("password",Keys.createEcKeyPair()).getVersion mustEqual 3
    }

    "loadCredentials correctly" in {
      val wallet = EthereumWalletUtils().createWallet("password",Keys.createEcKeyPair())
      val credentials = EthereumWalletUtils().loadCredentials("password", wallet)
      credentials.getAddress mustEqual "0x" + wallet.getAddress
      ok
    }

    "loadCredentials incorrectly" in {
      val wallet = EthereumWalletUtils().createWallet("password",Keys.createEcKeyPair())
      val b = try {
        EthereumWalletUtils().loadCredentials("wrong_password", wallet)
        false
      } catch {
        case ex: org.web3j.crypto.exception.CipherException => ex.getMessage == "Invalid password provided"
        case _: Exception => false
      }
      b mustEqual true
    }
  }
}
