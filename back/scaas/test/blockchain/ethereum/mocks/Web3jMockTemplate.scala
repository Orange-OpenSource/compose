package blockchain.ethereum.mocks

import io.reactivex.Flowable
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.request.ShhFilter
import org.web3j.protocol.core.methods.response.admin.{AdminDataDir, AdminNodeInfo, AdminPeers}
import org.web3j.protocol.core.methods.response._
import org.web3j.protocol.core.{BatchRequest, DefaultBlockParameter, Request}
import org.web3j.protocol.websocket.events.{LogNotification, NewHeadsNotification}

import java.math.BigInteger
import java.util

/**
 * This class is a hack to avoid mockito problem with java templated class converted to scala.
 */
class Web3jMockTemplate extends Web3j {
  override def shutdown(): Unit = ???

  override def newBatch(): BatchRequest = ???

  override def web3ClientVersion(): Request[?, Web3ClientVersion] = ???

  override def web3Sha3(data: String): Request[?, Web3Sha3] = ???

  override def netVersion(): Request[?, NetVersion] = ???

  override def netListening(): Request[?, NetListening] = ???

  override def netPeerCount(): Request[?, NetPeerCount] = ???

  override def adminNodeInfo(): Request[?, AdminNodeInfo] = ???

  override def adminPeers(): Request[?, AdminPeers] = ???

  override def ethProtocolVersion(): Request[?, EthProtocolVersion] = ???

  override def ethChainId(): Request[?, EthChainId] = ???

  override def ethCoinbase(): Request[?, EthCoinbase] = ???

  override def ethSyncing(): Request[?, EthSyncing] = ???

  override def ethMining(): Request[?, EthMining] = ???

  override def ethHashrate(): Request[?, EthHashrate] = ???

  override def ethGasPrice(): Request[?, EthGasPrice] = ???

  override def ethAccounts(): Request[?, EthAccounts] = ???

  override def ethBlockNumber(): Request[?, EthBlockNumber] = ???

  override def ethGetBalance(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetBalance] = ???

  override def ethGetStorageAt(address: String, position: BigInteger, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetStorageAt] = ???

  override def ethGetTransactionCount(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetTransactionCount] = ???

  override def ethGetBlockTransactionCountByHash(blockHash: String): Request[?, EthGetBlockTransactionCountByHash] = ???

  override def ethGetBlockTransactionCountByNumber(defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetBlockTransactionCountByNumber] = ???

  override def ethGetUncleCountByBlockHash(blockHash: String): Request[?, EthGetUncleCountByBlockHash] = ???

  override def ethGetUncleCountByBlockNumber(defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetUncleCountByBlockNumber] = ???

  override def ethGetCode(address: String, defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetCode] = ???

  override def ethSign(address: String, sha3HashOfDataToSign: String): Request[?, EthSign] = ???

  override def ethSendTransaction(transaction: org.web3j.protocol.core.methods.request.Transaction): Request[?, EthSendTransaction] = ???

  override def ethSendRawTransaction(signedTransactionData: String): Request[?, EthSendTransaction] = ???

  override def ethCall(transaction: org.web3j.protocol.core.methods.request.Transaction , defaultBlockParameter: DefaultBlockParameter): Request[?, EthCall] = ???

  override def ethEstimateGas(transaction: org.web3j.protocol.core.methods.request.Transaction ): Request[?, EthEstimateGas] = ???

  override def ethGetBlockByHash(blockHash: String, returnFullTransactionObjects: Boolean): Request[?, EthBlock] = ???

  override def ethGetBlockByNumber(defaultBlockParameter: DefaultBlockParameter, returnFullTransactionObjects: Boolean): Request[?, EthBlock] = ???

  override def ethGetTransactionByHash(transactionHash: String): Request[?, EthTransaction] = ???

  override def ethGetTransactionByBlockHashAndIndex(blockHash: String, transactionIndex: BigInteger): Request[?, EthTransaction] = ???

  override def ethGetTransactionByBlockNumberAndIndex(defaultBlockParameter: DefaultBlockParameter, transactionIndex: BigInteger): Request[?, EthTransaction] = ???

  override def ethGetTransactionReceipt(transactionHash: String): Request[?, EthGetTransactionReceipt] = ???

  override def ethGetUncleByBlockHashAndIndex(blockHash: String, transactionIndex: BigInteger): Request[?, EthBlock] = ???

  override def ethGetUncleByBlockNumberAndIndex(defaultBlockParameter: DefaultBlockParameter, transactionIndex: BigInteger): Request[?, EthBlock] = ???

  override def ethGetCompilers(): Request[?, EthGetCompilers] = ???

  override def ethCompileLLL(sourceCode: String): Request[?, EthCompileLLL] = ???

  override def ethCompileSolidity(sourceCode: String): Request[?, EthCompileSolidity] = ???

  override def ethCompileSerpent(sourceCode: String): Request[?, EthCompileSerpent] = ???

  override def ethNewFilter(ethFilter: org.web3j.protocol.core.methods.request.EthFilter): Request[?, org.web3j.protocol.core.methods.response.EthFilter] = ???

  override def ethNewBlockFilter(): Request[?, org.web3j.protocol.core.methods.response.EthFilter] = ???

  override def ethNewPendingTransactionFilter(): Request[?, org.web3j.protocol.core.methods.response.EthFilter] = ???

  override def ethUninstallFilter(filterId: BigInteger): Request[?, EthUninstallFilter] = ???

  override def ethGetFilterChanges(filterId: BigInteger): Request[?, EthLog] = ???

  override def ethGetFilterLogs(filterId: BigInteger): Request[?, EthLog] = ???

  override def ethGetLogs(ethFilter: org.web3j.protocol.core.methods.request.EthFilter): Request[?, EthLog] = ???

  override def ethGetWork(): Request[?, EthGetWork] = ???

  override def ethSubmitWork(nonce: String, headerPowHash: String, mixDigest: String): Request[?, EthSubmitWork] = ???

  override def ethSubmitHashrate(hashrate: String, clientId: String): Request[?, EthSubmitHashrate] = ???

  override def ethGetBaseFeePerBlobGas(): java. math. BigInteger = ???

  override def dbPutString(databaseName: String, keyName: String, stringToStore: String): Request[?, DbPutString] = ???

  override def dbGetString(databaseName: String, keyName: String): Request[?, DbGetString] = ???

  override def dbPutHex(databaseName: String, keyName: String, dataToStore: String): Request[?, DbPutHex] = ???

  override def dbGetHex(databaseName: String, keyName: String): Request[?, DbGetHex] = ???

  override def shhPost(shhPost: org.web3j.protocol.core.methods.request.ShhPost): Request[?, org.web3j.protocol.core.methods.response.ShhPost] = ???

  override def shhVersion(): Request[?, ShhVersion] = ???

  override def shhNewIdentity(): Request[?, ShhNewIdentity] = ???

  override def shhHasIdentity(identityAddress: String): Request[?, ShhHasIdentity] = ???

  override def shhNewGroup(): Request[?, ShhNewGroup] = ???

  override def shhAddToGroup(identityAddress: String): Request[?, ShhAddToGroup] = ???

  override def shhNewFilter(shhFilter: ShhFilter): Request[?, ShhNewFilter] = ???

  override def shhUninstallFilter(filterId: BigInteger): Request[?, ShhUninstallFilter] = ???

  override def shhGetFilterChanges(filterId: BigInteger): Request[?, ShhMessages] = ???

  override def shhGetMessages(filterId: BigInteger): Request[?, ShhMessages] = ???

  override def ethLogFlowable(ethFilter: org.web3j.protocol.core.methods.request.EthFilter): Flowable[Log] = ???

  override def ethBlockHashFlowable(): Flowable[String] = ???

  override def ethPendingTransactionHashFlowable(): Flowable[String] = ???

  override def transactionFlowable(): Flowable[org.web3j.protocol.core.methods.response.Transaction] = ???

  override def pendingTransactionFlowable(): Flowable[org.web3j.protocol.core.methods.response.Transaction] = ???

  override def blockFlowable(fullTransactionObjects: Boolean): Flowable[EthBlock] = ???

  override def replayPastBlocksFlowable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter, fullTransactionObjects: Boolean): Flowable[EthBlock] = ???

  override def replayPastBlocksFlowable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter, fullTransactionObjects: Boolean, ascending: Boolean): Flowable[EthBlock] = ???

  override def replayPastBlocksFlowable(startBlock: DefaultBlockParameter, fullTransactionObjects: Boolean, onCompleteFlowable: Flowable[EthBlock]): Flowable[EthBlock] = ???

  override def replayPastBlocksFlowable(startBlock: DefaultBlockParameter, fullTransactionObjects: Boolean): Flowable[EthBlock] = ???

  override def replayPastTransactionsFlowable(startBlock: DefaultBlockParameter, endBlock: DefaultBlockParameter): Flowable[org.web3j.protocol.core.methods.response.Transaction] = ???

  override def replayPastTransactionsFlowable(startBlock: DefaultBlockParameter): Flowable[org.web3j.protocol.core.methods.response.Transaction] = ???

  override def replayPastAndFutureBlocksFlowable(startBlock: DefaultBlockParameter, fullTransactionObjects: Boolean): Flowable[EthBlock] = ???

  override def replayPastAndFutureTransactionsFlowable(startBlock: DefaultBlockParameter): Flowable[org.web3j.protocol.core.methods.response.Transaction] = ???

  override def newHeadsNotifications(): Flowable[NewHeadsNotification] = ???

  override def logsNotifications(addresses: util.List[String], topics: util.List[String]): Flowable[LogNotification] = ???

  override def adminAddPeer(url: String): Request[?, BooleanResponse] = ???

  override def adminRemovePeer(url: String): Request[?, BooleanResponse] = ???

  override def adminDataDir(): Request[?, AdminDataDir] = ???

  override def txPoolStatus(): Request[?, TxPoolStatus] = ???

  override def ethMaxPriorityFeePerGas: Request[?, EthMaxPriorityFeePerGas] = ???

  override def ethBaseFee: Request[?, EthBaseFee] = ???

  override def ethFeeHistory(blockCount: Int, newestBlock: DefaultBlockParameter, rewardPercentiles: util.List[java.lang.Double]): Request[?, EthFeeHistory] = ???

  override def ethGetBlockReceipts(defaultBlockParameter: DefaultBlockParameter): Request[?, EthGetBlockReceipts] = ???

  override def ethGetProof(address: String, storageKeys: util.List[String], quantity: String): Request[?, EthGetProof] = ???

}
