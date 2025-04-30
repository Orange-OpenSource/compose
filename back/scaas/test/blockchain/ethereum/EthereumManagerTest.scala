package blockchain.ethereum

import blockchain.ethereum.mocks.Web3jMockTemplate
import common.exceptions.BlockchainException
import org.mockito.Mockito._
import org.web3j.crypto.Keys
import org.web3j.protocol.core.methods.response._
import org.web3j.protocol.core.methods.response.admin.AdminPeers
import org.web3j.protocol.core.{DefaultBlockParameter, DefaultBlockParameterName, Request}
import play.api.Configuration
import play.api.test.PlaySpecification
import smart_contracts.controllers.blockchain.ethereum.EthereumManager
import smart_contracts.model.body
import smart_contracts.model.body.{Block, Log}
import wallets.utils.EthereumWalletUtils

import java.math.BigInteger
import java.util
import java.util.Optional

class EthereumManagerTest extends PlaySpecification {

  "EthereumManagerTest" should {

    "invoke a read only function (invokeView)" in {
      val conf = mock(classOf[Configuration])
      val wallet = EthereumWalletUtils().createWallet("password", Keys.createEcKeyPair())
      val credentials = EthereumWalletUtils().loadCredentials("password", wallet)

      val r = mock(classOf[Request[?, EthCall]])
      val requAccounts = mock(classOf[Request[?, EthAccounts]])
      val accounts = mock(classOf[EthAccounts])
      when(requAccounts.send()).thenReturn(accounts)
      val acccs = new util.ArrayList[String]()
      acccs.add("0x576B0794736878637b829B2EDFAD7C1223CF6F1A")
      when(accounts.getAccounts).thenReturn(acccs)

      class Web3jMock extends Web3jMockTemplate {
        override def ethCall(transaction: org.web3j.protocol.core.methods.request.Transaction, defaultBlockParameter: DefaultBlockParameter): Request[?, EthCall] = {
          val inner_transaction: org.web3j.protocol.core.methods.request.Transaction = org.web3j.protocol.core.methods.request.Transaction.createEthCallTransaction(
            credentials.getAddress,
            "0x576B0794736878637b829B2EDFAD7C1223CF6F1A",
            "59a501a50000000000000000000000000000000000000000000000000000000000000000"
          )

          transaction.getValue mustEqual inner_transaction.getValue
          defaultBlockParameter mustEqual DefaultBlockParameterName.LATEST
          r
        }

        override def ethAccounts(): Request[?, EthAccounts] = {
          requAccounts
        }
      }

      val web3j = new Web3jMock
      val em = new EthereumManager("abf-testnet", conf)(web3j)

      val ethCall = mock(classOf[EthCall])
      when(r.send()).thenReturn(ethCall)
      when(ethCall.isReverted).thenReturn(false)
      when(ethCall.getValue).thenReturn("0x000000000000000000000000000000000000000000000000000000000000002a")
      val result = em.invokeView("59a501a50000000000000000000000000000000000000000000000000000000000000000", "0x576B0794736878637b829B2EDFAD7C1223CF6F1A")
      result.get mustEqual "0x000000000000000000000000000000000000000000000000000000000000002a"
    }

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

      val txCount: EthGetTransactionCount =  mock(classOf[EthGetTransactionCount])
      when(r.send).thenReturn(txCount)
      when(txCount.getTransactionCount).thenReturn(BigInt(5).bigInteger)

      val v: EthGasPrice = mock(classOf[EthGasPrice])

      when(gasPrice.send()).thenReturn(v)
      when(v.getGasPrice).thenReturn(BigInt(42).bigInteger)

      val chid : EthChainId = mock(classOf[EthChainId])
      when(chainId.send()).thenReturn(chid)
      when (chid.getChainId).thenReturn(BigInt(1337).bigInteger)
      val a: EthEstimateGas = mock(classOf[EthEstimateGas])

      when(estimateGas.send()).thenReturn(a)
      when(a.getAmountUsed).thenReturn(BigInt(42).bigInteger)

      val tx: EthSendTransaction = mock(classOf[EthSendTransaction])

      when(rawTx.send()).thenReturn(tx)
      when(tx.getTransactionHash).thenReturn("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")

      val web3j = new Web3jMock
      val em = new EthereumManager("abf-testnet", conf)(web3j)
      val result = em.invoke("59a501a50000000000000000000000000000000000000000000000000000000000000000", 0,"0x576B0794736878637b829B2EDFAD7C1223CF6F1A")(credentials)

      result.isSuccess mustEqual true
      result.get mustEqual "ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5"
    }

    "blockchainName" in {
      val conf = mock(classOf[Configuration])
      val em = EthereumManager("abf-testnet", conf)
      em.blockchainName mustEqual "abf-testnet"
    }

    "return the transaction receipt" in {
      val conf = mock(classOf[Configuration])

      val tr:  Request[?, EthGetTransactionReceipt] = mock(classOf[Request[?, EthGetTransactionReceipt] ])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetTransactionReceipt(transactionHash: String): Request[?, EthGetTransactionReceipt] = {
          tr
        }
      }

      val r: EthGetTransactionReceipt = mock(classOf[EthGetTransactionReceipt])
      when(tr.send()).thenReturn(r)
      val t: TransactionReceipt = mock(classOf[TransactionReceipt])
      when(r.getTransactionReceipt).thenReturn(Optional.of(t))
      when(t.getTransactionHash).thenReturn("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")
      when(t.isStatusOK).thenReturn(true)
      when(t.getGasUsedRaw).thenReturn("42")
      when(t.getContractAddress).thenReturn("0x576B0794736878637b829B2EDFAD7C1223CF6F1A")
      val web3j = new Web3jMock

      val log :body.Log = Log(Some(true),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(List(null)))
      val transaction :body.Transaction = body.Transaction(Some("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5"),Some(null),Some(null),
        Some(null),Some(null),Some("42"),Some("0x576B0794736878637b829B2EDFAD7C1223CF6F1A"),Some(null),Some(null),
        Some(null),Some(null),Some(List()),Some(null),Some(null),Some(null),Some(null))
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val receipt = em.transactionReceipt("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")
      receipt.get mustEqual transaction
    }
    "fails to return the transaction receipt" in {
      val conf = mock(classOf[Configuration])

      val tr: Request[?, EthGetTransactionReceipt] = mock(classOf[Request[?, EthGetTransactionReceipt]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetTransactionReceipt(transactionHash: String): Request[?, EthGetTransactionReceipt] = {
          tr
        }
      }

      val r: EthGetTransactionReceipt = mock(classOf[EthGetTransactionReceipt])
      when(tr.send()).thenReturn(r)
      val t: TransactionReceipt = mock(classOf[TransactionReceipt])
      when(r.getTransactionReceipt).thenReturn(Optional.of(t))
      when(t.getTransactionHash).thenReturn("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")
      when(t.isStatusOK).thenReturn(false)
      when(t.getGasUsedRaw).thenReturn("42")
      when(t.getContractAddress).thenReturn("0x576B0794736878637b829B2EDFAD7C1223CF6F1A")
      val web3j = new Web3jMock

      val log: body.Log = Log(Some(true), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(List(null)))
      val transaction: body.Transaction = body.Transaction(Some("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5"), Some(null), Some(null),
        Some(null), Some(null), Some("42"), Some("0x576B0794736878637b829B2EDFAD7C1223CF6F1A"), Some(null), Some(null),
        Some(null), Some(null), Some(List()), Some(null), Some(null), Some(null), Some(null))
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val receipt = em.transactionReceipt("ec864fe99b539704b8872ac591067ef22d836a8d942087f2dba274b301ebe6e5")
      receipt.get must throwA(BlockchainException(null))
    }


    "get balance " in {
      val conf = mock(classOf[Configuration])
      val bal : Request[?, EthGetBalance] = mock(classOf[Request[?, EthGetBalance]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetBalance(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetBalance] = {
          bal
        }
      }
      val ethb : EthGetBalance = mock(classOf[EthGetBalance])
      when(bal.send()).thenReturn(ethb)
      when(ethb.getBalance).thenReturn(new BigInteger("2000000000000"))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet",conf)(web3j)
      val balance = em.getBalance("0x0000")

      balance mustEqual "2000000000000"
    }

    "get block number " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthBlockNumber] = mock(classOf[Request[?, EthBlockNumber]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethBlockNumber(): Request[?, EthBlockNumber] = {
          bn
        }
      }
      val ethb: EthBlockNumber = mock(classOf[EthBlockNumber])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getBlockNumber).thenReturn(new BigInteger("2000000000000"))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getBlockNumber

      balance mustEqual "2000000000000"
    }
    "get gas price " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthGasPrice] = mock(classOf[Request[?, EthGasPrice]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGasPrice(): Request[?, EthGasPrice] = {
          bn
        }
      }
      val ethb: EthGasPrice = mock(classOf[EthGasPrice])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getGasPrice).thenReturn(new BigInteger("2000000000000"))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getGasPrice

      balance mustEqual "2000000000000"
    }
    "get chain id " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthChainId] = mock(classOf[Request[?, EthChainId]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethChainId(): Request[?, EthChainId] = {
          bn
        }
      }
      val ethb: EthChainId = mock(classOf[EthChainId])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getChainId).thenReturn(new BigInteger("1337"))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getChainId

      balance mustEqual "1337"
    }
    "get block by hash " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthBlock] = mock(classOf[Request[?, EthBlock]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetBlockByHash(blockHash: String, returnFullTransactionObjects: Boolean): Request[?, EthBlock] = {
          bn
        }
      }
      val ethb: EthBlock = mock(classOf[EthBlock])
      val ethbb: EthBlock.Block = mock(classOf[EthBlock.Block])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getBlock).thenReturn(ethbb)
      val block :Block = new Block(Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(null),Some(List()),Some(List()),Some(null))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getBlockByHash("0x0000")

      balance mustEqual block
    }
    "get block by number " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthBlock] = mock(classOf[Request[?, EthBlock]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetBlockByNumber(defaultBlockParameter: DefaultBlockParameter, returnFullTransactionObjects: Boolean): Request[?, EthBlock] = {
          bn
        }
      }
      val ethb: EthBlock = mock(classOf[EthBlock])
      val ethbb: EthBlock.Block = mock(classOf[EthBlock.Block])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getBlock).thenReturn(ethbb)
      val block: Block = new Block(Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(null), Some(List()), Some(List()), Some(null))
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getBlockByNumber("1")

      balance mustEqual block
    }
    "get code at " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthGetCode] = mock(classOf[Request[?, EthGetCode]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethGetCode(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetCode] = {
          bn
        }
      }
      val ethb: EthGetCode = mock(classOf[EthGetCode])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getCode).thenReturn("code")
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getCodeAt("0x0000")

      balance mustEqual "code"
    }
   /* "get peers number" in {
      val conf = mock[Configuration]
      val bn: Request[_, AdminPeers] = mock[Request[_, AdminPeers]]
      class Web3jMock extends Web3jMockTemplate {
        override def adminPeers(): Request[_, AdminPeers] = {
          bn
        }
      }
      val ethb: AdminPeers = mock[AdminPeers]
      when(bn.send()).thenReturn(ethb)
      //when(ethb.getResult).thenReturn()
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getPeersNumber

      balance mustEqual 1
    }*/
    "get protocol version " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, EthProtocolVersion] = mock(classOf[Request[?, EthProtocolVersion]])
      class Web3jMock extends Web3jMockTemplate {
        override def ethProtocolVersion(): Request[?, EthProtocolVersion] = {
          bn
        }
      }
      val ethb: EthProtocolVersion = mock(classOf[EthProtocolVersion])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getProtocolVersion).thenReturn("version")
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.getProtocolVersion

      balance mustEqual "version"
    }
    "get web3 version " in {
      val conf = mock(classOf[Configuration])
      val bn: Request[?, Web3ClientVersion] = mock(classOf[Request[?, Web3ClientVersion]])
      class Web3jMock extends Web3jMockTemplate {
        override def web3ClientVersion(): Request[?, Web3ClientVersion] = {
          bn
        }
      }
      val ethb: Web3ClientVersion = mock(classOf[Web3ClientVersion])
      when(bn.send()).thenReturn(ethb)
      when(ethb.getWeb3ClientVersion).thenReturn("version")
      val web3j = new Web3jMock
      val em = new EthereumManager("abfTestnet", conf)(web3j)
      val balance = em.web3Version()

      balance mustEqual "version"
    }
  }
}
