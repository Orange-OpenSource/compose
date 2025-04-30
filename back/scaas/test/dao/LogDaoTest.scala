package dao

import common.dao.AbstractDao
import smart_contracts.dao.LogDao
import smart_contracts.model.dao.LogProject

class LogDaoTest extends AbstractDaoTest[LogProject] {

  override val collectionName: String = "projectLogs"
  override val element: LogProject = LogProject(id, "project", "method", "1720006719", "my_log")
  override val updatedElement: LogProject = LogProject(id, "project", "method_1", "1720006720", "my_very_long_log")
  override val secondElement: LogProject = LogProject("_id2", "project_2", "method_2", "1720006758", "my_log2")
  override val dao: AbstractDao[LogProject] = new LogDao()
}
