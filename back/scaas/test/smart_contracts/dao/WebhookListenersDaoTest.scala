package smart_contracts.dao

import common.dao.AbstractDao
import dao.AbstractDaoTest
import smart_contracts.model.dao.WebhookListener

class WebhookListenersDaoTest extends AbstractDaoTest[WebhookListener] {
  override val element: WebhookListener = WebhookListener("_id", "userId", "ethereum", "address", "test", "url")
  override val updatedElement: WebhookListener = WebhookListener("_id", "userId", "ethereum", "address", "test2", "url")
  override val secondElement: WebhookListener = WebhookListener("_id3", "userId", "ethereum", "address3", "test3", "url3")
  override val collectionName: String = "http-callback-hooks"
  override val dao: AbstractDao[WebhookListener] = new WebhookListenersDao()
}
