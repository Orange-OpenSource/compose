package dao

import common.dao.AbstractDao
import org.joda.time.DateTime
import smart_contracts.dao.OngoingDeploymentDAO
import smart_contracts.model.dao.OngoingDeployment

class OngoingDeploymentDaoTest extends AbstractDaoTest[OngoingDeployment] {

  override val collectionName: String = "ongoingDeployment"
  override val element: OngoingDeployment = OngoingDeployment("_id", "ethereum", "userId", DateTime.now().getMillis.toString)
  override val updatedElement: OngoingDeployment = OngoingDeployment("_id", "alastria", "userId", DateTime.now().getMillis.toString)
  override val secondElement: OngoingDeployment = OngoingDeployment("_id2", "ethereum", "userId", DateTime.now().getMillis.toString)
  override val dao: AbstractDao[OngoingDeployment] = new OngoingDeploymentDAO()
}
