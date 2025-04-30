package blockchain

import com.typesafe.config.ConfigFactory
import org.mockito.Mockito._
import play.api.Configuration
import play.api.test.PlaySpecification
import wallets.controllers.factory.WalletFactory

class WalletFactoryTest extends PlaySpecification{

  "walletFacotry" should {

    "build" in {

      val walletFactory : WalletFactory = new WalletFactory
      val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
      val wm = walletFactory.build(configuration,"ethereum")
      wm.getClass.getName.contains("EthereumWalletManager")
    }
    "build" in {

      val walletFactory : WalletFactory = new WalletFactory
      val configuration: Configuration = new Configuration(ConfigFactory.load("application.test.conf"))
      val wm = walletFactory.build( configuration, "alastria")
      wm.getClass.getName.contains("EthereumWalletManager")
    }
  }
}



