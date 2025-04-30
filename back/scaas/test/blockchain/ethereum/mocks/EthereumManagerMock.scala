package blockchain.ethereum.mocks


import org.web3j.crypto.Credentials
import smart_contracts.controllers.blockchain.BlockchainManager
import smart_contracts.model.body.{Block, Log, Transaction}

import scala.util.Try


class EthereumManagerMock() extends BlockchainManager {
  override val blockchainName: String = "ethereum"

  override def invokeView(encodedFunction: String, contractAddress: String, senderAddress: String): Try[String] = {

    Try("")
  }

  override def invoke(encodedFunction: String, tokenValue: BigInt,contractAddress: String)(credentials: Credentials): Try[String] = {
    Try("")
  }

  override def transactionReceipt(transactionHash: String): Try[Transaction] = {
    val log: Log = new smart_contracts.model.body.Log(
      Some(false),
      Some("16"),
      Some("4"),
      Some("0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc"),
      Some("0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1"),
      Some("8726746"),
      Some("0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8"),
      Some("0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000"),
      Some("1"),
      Some(List("0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777")))
      Try(Transaction(Some("0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc"),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List(log)),Some(""),Some(""),Some(""),Some("")))
  }

  override def getBlockNumber: String = {
    "1"
  }

  override def getBlockByNumber(number: String): Block = {
    Block(Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List("")),Some(List("")),Some(""))
  }

  override def getBlockByHash(hash: String): Block = {
    Block(Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(""),Some(List("")),Some(List("")),Some(""))
  }

  override def getCodeAt(address: String): String = {
    "1"
  }

  override def getPeersNumber: Int = {
    1
  }

  override def getProtocolVersion: String = {
    "1"
  }

  override def web3Version(): String = {
    "1"

  }

  override def getChainId: String = {

    "1337"
  }

  override def getGasPrice: String = {

    "price"
  }
}


