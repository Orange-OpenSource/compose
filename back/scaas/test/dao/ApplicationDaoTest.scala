package dao

import applications.dao.ApplicationDao
import applications.model.dao.Application
import common.dao.AbstractDao

class ApplicationDaoTest extends AbstractDaoTest[Application] {
  override val element: Application = Application(Some(id), "cayMyName", None, "API_KEY_SECRET_TOP", "salt", "ethereum", "walletID")
  override val updatedElement: Application = Application(Some(id), "cayMyName", Some("127.0.0.1"), "API_KEY_SECRET_NEW", "salt", "ethereum", "walletID")
  override val secondElement: Application = Application(Some("id2"), "cayMyNamebis", Some("127.0.0.1"), "API_KEY_SECRET_NEW", "salt", "ethereum", "walletID")
  override val collectionName: String = "applications"
  override val dao: AbstractDao[Application] = new ApplicationDao()
}
