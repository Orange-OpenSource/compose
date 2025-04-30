package events.controller.listeners.concrete

import blockchain.ethereum.mocks.Web3jMockTemplate
import io.reactivex.{BackpressureStrategy, Flowable, FlowableEmitter}
import mockws.MockWS
import mockws.MockWSHelpers.Action
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{mock, when}
import org.web3j.protocol.core.Request
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.response.{EthBlockNumber, EthLog, Log}
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.Injector
import play.api.libs.ws.WSClient
import play.api.mvc.Results.Ok
import smart_contracts.controllers.blockchain.ethereum.{EthereumEventListener, EthereumEventManager}
import smart_contracts.controllers.blockchain.{EventListener, EventWebhookListenerTest}
import smart_contracts.dao.{AbiDao, WebhookListenersDao}
import smart_contracts.model.dao.{Abi, WebhookListener}

import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import scala.jdk.CollectionConverters.SeqHasAsJava
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

class EthereumEventWebhookListenerTest extends EventWebhookListenerTest {

  lazy val injector: Injector = new GuiceApplicationBuilder().injector()
  val webhookListenersDao: WebhookListenersDao = mock(classOf[WebhookListenersDao])
  val wSClient: WSClient = mock(classOf[WSClient])
  implicit lazy val executionContext: ExecutionContext = injector.instanceOf[ExecutionContext]
  val conf: Configuration = mock(classOf[Configuration])
  val abiDao: AbiDao = mock(classOf[AbiDao])
  val configuration: Configuration = mock(classOf[Configuration])
  when(configuration.get[Int]("blockchain.abf-testnet.logs")).thenReturn(1)
  val abi: Abi = Abi(
    Some("_id1"),
    "my_abi",
    "abf-testnet",
    """
    [
    {
            "anonymous": false,
            "inputs": [
              {
                "indexed": false,
                "internalType": "string",
                "name": "messenger",
                "type": "string"
              },
              {
                "indexed": false,
                "internalType": "string",
                "name": "username",
                "type": "string"
              },
              {
                "indexed": false,
                "internalType": "string",
                "name": "nome",
                "type": "string"
              },
              {
                "indexed": false,
                "internalType": "string",
                "name": "cognome",
                "type": "string"
              },
              {
                "indexed": false,
                "internalType": "string",
                "name": "email",
                "type": "string"
              }
            ],
            "name": "Winner",
            "type": "event"
          },
    {
      "inputs": [
        {
          "internalType": "uint8",
          "name": "value",
          "type": "uint8"
        }
      ],
      "name": "encodedEvent",
      "outputs": [
        {
          "internalType": "bytes",
          "name": "",
          "type": "bytes"
        }
      ],
      "stateMutability": "pure",
      "type": "function"
    }
  ]
  """,
    "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8",
    "userId")

  val log = new Log(
    false,
    "16",
    "4",
    "0xb4c92a37994ef680b33a5c6c9084926e10f26d5c659691ee5821712d31a5fbdc",
    "0x2de8d9ff07b91523359c08a0004131d5839a7d4ff5aa1bd00843777a5e096ca1",
    "8726746",
    "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8",
    "0x00000000000000000000000000000000000000000000000000000000000000a000000000000000000000000000000000000000000000000000000000000000e00000000000000000000000000000000000000000000000000000000000000120000000000000000000000000000000000000000000000000000000000000016000000000000000000000000000000000000000000000000000000000000001a0000000000000000000000000000000000000000000000000000000000000000c496c2056696e6369746f7265000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177716500000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000477657771000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000003717765000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000047177657100000000000000000000000000000000000000000000000000000000",
    "1",
    List("0x8cdd8faf489944941fc651bffdc4a404b57bddc1c35eccf8531de89a4422d777").asJava)
  val rblockNumber: Request[?, EthBlockNumber] = mock(classOf[Request[?, EthBlockNumber]])
  val blockNumber: EthBlockNumber = mock(classOf[EthBlockNumber])
  val rEthLog: Request[?, EthLog] = mock(classOf[Request[?, EthLog]])
  val ethLog: EthLog = mock(classOf[EthLog])
  val web3j: Web3jMockTemplate = new Web3jMockTemplate {

    override def ethLogFlowable(ethFilter: EthFilter): Flowable[Log] = {
      Flowable.create((emitter: FlowableEmitter[Log]) => {
        //Leaving open
      }, BackpressureStrategy.BUFFER)
    }
    override def ethBlockNumber(): Request[?, EthBlockNumber] = rblockNumber
    override def ethGetLogs(ethFilter: org.web3j.protocol.core.methods.request.EthFilter): Request[?, EthLog] = rEthLog
  }

  override val eventListener: EventListener =
    EthereumEventListener(
      "abf-testnet",
      EthereumEventManager(abiDao, "abf-testnet", web3j, executionContext),
      webhookListenersDao,
      wSClient,
      conf,
      executionContext, web3j)

  "EthereumEventListener" should {
    "Decode correctly an event" in {

      val flowWeb3 = new Web3jMockTemplate {

        override def ethLogFlowable(ethFilter: EthFilter): Flowable[Log] = {
          Flowable.create((emitter: FlowableEmitter[Log]) => {

          }, BackpressureStrategy.BUFFER)
        }
      }

      when(webhookListenersDao.find(any, any))
        .thenReturn(Future.successful(
          List[WebhookListener](WebhookListener("_id", "userId", "abf-testnet", "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8", "Winner", "https://callback.orange.com/compose"))
        ))

      val route = mockws.Route {
        case (POST, "https://callback.orange.com/compose") => Action { request =>
          val body = request.body.asJson.get
          body.toString() mustEqual "{\"address\":\"0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8\",\"blockNumber\":8726746,\"name\":\"Winner\",\"parameters\":[{\"name\":\"nome\",\"type\":\"string\",\"value\":\"wewq\"},{\"name\":\"username\",\"type\":\"string\",\"value\":\"qwqe\"},{\"name\":\"messenger\",\"type\":\"string\",\"value\":\"Il Vincitore\"},{\"name\":\"cognome\",\"type\":\"string\",\"value\":\"qwe\"},{\"name\":\"email\",\"type\":\"string\",\"value\":\"qweq\"}]}"
          Ok("")
        }
      }
      val ws = MockWS {
        route
      }


      when(abiDao.findOneBy("address")("0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8")).thenReturn(Future.successful(abi))

      val evmEventListener: EventListener =
        EthereumEventListener(
          "abf-testnet",
          EthereumEventManager(abiDao, "abf-testnet", web3j, executionContext),
          webhookListenersDao,
          ws,
          conf,
          executionContext, flowWeb3)

      Await.ready(evmEventListener.notifyListeners(log, "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8"), Duration.Inf)
      route.called mustEqual true
    }
    "test poll logs" in {

      val flowWeb3 = new Web3jMockTemplate {

        override def ethLogFlowable(ethFilter: EthFilter): Flowable[Log] = {
          Flowable.create((emitter: FlowableEmitter[Log]) => {

          }, BackpressureStrategy.BUFFER)
        }
        override def ethBlockNumber(): Request[?, EthBlockNumber] = rblockNumber
      }

      when(rblockNumber.send()).thenReturn(blockNumber)
      when(blockNumber.getBlockNumber()).thenReturn( new BigInteger("1000"))
      when(rEthLog.sendAsync().thenAccept).thenReturn(CompletableFuture.completedFuture(ethLog))


      when(webhookListenersDao.find(any, any))
        .thenReturn(Future.successful(
          List[WebhookListener](WebhookListener("_id", "userId", "abf-testnet", "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8", "Winner", "https://callback.orange.com/compose"))
        ))

      val route = mockws.Route {
        case (POST, "https://callback.orange.com/compose") => Action { request =>
          val body = request.body.asJson.get
          body.toString() mustEqual "{\"address\":\"0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8\",\"blockNumber\":8726746,\"name\":\"Winner\",\"parameters\":[{\"name\":\"nome\",\"type\":\"string\",\"value\":\"wewq\"},{\"name\":\"username\",\"type\":\"string\",\"value\":\"qwqe\"},{\"name\":\"messenger\",\"type\":\"string\",\"value\":\"Il Vincitore\"},{\"name\":\"cognome\",\"type\":\"string\",\"value\":\"qwe\"},{\"name\":\"email\",\"type\":\"string\",\"value\":\"qweq\"}]}"
          Ok("")
        }
      }
      val ws = MockWS {
        route
      }


      when(abiDao.findOneBy("address")("0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8")).thenReturn(Future.successful(abi))

      val evmEventListener: EventListener =
        EthereumEventListener(
          "abf-testnet",
          EthereumEventManager(abiDao, "abf-testnet", web3j, executionContext),
          webhookListenersDao,
          ws,
          conf,
          executionContext, flowWeb3)


      eventListener.pollLogs(30)
      Await.ready(evmEventListener.notifyListeners(log, "0x75508672C8C155b06a0Bc82e8CA9aB967c9a17d8"), Duration.Inf)
      route.called mustEqual true
    }
  }
}
