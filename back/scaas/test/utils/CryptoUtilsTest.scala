package utils

import common.utils.CryptoUtils
import org.mockito.Mockito.{mock, when}
import org.specs2.mutable.Specification
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector

import scala.concurrent.ExecutionContext

class CryptoUtilsTest extends Specification {

  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  val configuration: Configuration = mock(classOf[Configuration])
  when(configuration.get[String]("blockchain.wallet.master-key")).thenReturn("azerty1234567890")
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]

  "CryptoUtilsTest" should {
    "derivateKey correctly with correct input" in {
      val cryptoTool = CryptoUtils(configuration = configuration)
      val key = cryptoTool.retrieveWalletKey("randomWalletSalt", "randomClientSalt")
      key mustEqual "c09b0bd8c4aad0e09d442cd8580c3a70ce3caa8ab05a45588b31a81735f70326"
    }

    "derivateKey incorrectly with incorrect input" in {
      val cryptoTool = CryptoUtils(configuration = configuration)
      val key = cryptoTool.retrieveWalletKey("randomWalletSalt", "wrongClientSalt")
      key mustNotEqual "c09b0bd8c4aad0e09d442cd8580c3a70ce3caa8ab05a45588b31a81735f70326"
    }

    "create a safe token for the user" in {
      val cryptoTool = CryptoUtils(configuration = configuration)
      val key = cryptoTool.generateSafeToken(15)
      key.length mustEqual 30
    }


  }
}
