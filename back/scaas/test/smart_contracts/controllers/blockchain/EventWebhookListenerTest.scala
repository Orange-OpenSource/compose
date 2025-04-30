package smart_contracts.controllers.blockchain

import play.api.test.PlaySpecification

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

abstract class EventWebhookListenerTest extends PlaySpecification {

  val eventListener: EventListener

  "EventListenerTest" should {

    "dispose in less than 10 seconds" in {
      eventListener.isDisposed mustEqual false
      Await.result(eventListener.dispose(), 10.seconds) mustEqual true
      eventListener.isDisposed mustEqual true
    }
  }
}
