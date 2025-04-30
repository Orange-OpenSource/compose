package blockchain

import play.api.Configuration
import play.api.test.PlaySpecification
import org.mockito.Mockito._
import smart_contracts.controllers.blockchain.factory.BlockchainFactory

class BlockchainFactoryTest extends PlaySpecification {

  "blockchainFactory" should {

    "build" in {

      val blockchainFactory : BlockchainFactory = new BlockchainFactory
      val conf: Configuration = mock(classOf[Configuration])
      val bcm = blockchainFactory.build("ethereum",conf,"ethereum")
      bcm.getClass.getName.contains("EthereumManager")
    }
    "build" in {

      val blockchainFactory: BlockchainFactory = new BlockchainFactory
      val conf: Configuration = mock(classOf[Configuration])
      val bcm = blockchainFactory.build("ethereumlegacy", conf, "alastria")
      bcm.getClass.getName.contains("EthereumLegacyManager")
    }
    }
}
