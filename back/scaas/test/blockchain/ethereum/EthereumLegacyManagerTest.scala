package blockchain.ethereum

import blockchain.ethereum.mocks.Web3jMockTemplate
import org.mockito.Mockito._
import org.web3j.crypto.{ECKeyPair, Keys}
import org.web3j.protocol.core.methods.response._
import org.web3j.protocol.core.{DefaultBlockParameter, Request}
import play.api.Configuration
import play.api.test.PlaySpecification
import smart_contracts.controllers.blockchain.ethereum.EthereumLegacyManager
import wallets.utils.EthereumWalletUtils

import java.util

class EthereumLegacyManagerTest extends PlaySpecification {

  "EthereumLegacyManagerTest" should {

        "invoke" in {
          val conf = mock(classOf[Configuration])
          val wallet = EthereumWalletUtils().createWallet("password",Keys.createEcKeyPair())
          val credentials = EthereumWalletUtils().loadCredentials("password", wallet)

          val r = mock(classOf[Request[?, EthGetTransactionCount]])
          val gasPrice = mock(classOf[Request[?, EthGasPrice]])
          val estimateGas = mock(classOf[Request[?, EthEstimateGas]])
          val rawTx = mock(classOf[Request[?, EthSendTransaction]])
          val chainId = mock(classOf[Request[?, EthChainId]])

          class Web3jMock extends Web3jMockTemplate {
            override def ethGetTransactionCount(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetTransactionCount] = r

            override def ethGasPrice(): Request[?, EthGasPrice] = gasPrice

            override def ethChainId(): Request[?, EthChainId] = chainId

            override def ethEstimateGas(transaction: org.web3j.protocol.core.methods.request.Transaction): Request[?, EthEstimateGas] = estimateGas

            override def ethSendRawTransaction(signedTransactionData: String): Request[?, EthSendTransaction] = rawTx
          }

          val txCount: EthGetTransactionCount = mock(classOf[EthGetTransactionCount])
          when(r.send).thenReturn(txCount)
          when(txCount.getTransactionCount).thenReturn(BigInt(5).bigInteger)

          val v: EthGasPrice = mock(classOf[EthGasPrice])

          when(gasPrice.send()).thenReturn(v)
          when(v.getGasPrice).thenReturn(BigInt(42).bigInteger)

          val chid: EthChainId = mock(classOf[EthChainId])
          when(chainId.send()).thenReturn(chid)
          when(chid.getChainId).thenReturn(BigInt(1337).bigInteger)
          val a: EthEstimateGas = mock(classOf[EthEstimateGas])

          when(estimateGas.send()).thenReturn(a)
          when(a.getAmountUsed).thenReturn(BigInt(42).bigInteger)

          val tx: EthSendTransaction = mock(classOf[EthSendTransaction])

          when(rawTx.send()).thenReturn(tx)
          when(tx.getTransactionHash).thenReturn("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")

          val web3j = new Web3jMock
          val em = new EthereumLegacyManager("abf-testnet", conf)(web3j)
          val result = em.invoke("59a501a50000000000000000000000000000000000000000000000000000000000000000", 0, "0x576B0794736878637b829B2EDFAD7C1223CF6F1A")(credentials)

          result.isSuccess mustEqual true
          result.get mustEqual "ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5"
        }

      }
}
