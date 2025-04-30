package dao

import common.dao.AbstractDao
import users.dao.UserDao
import users.model.dao.User

class UserDaoTest extends AbstractDaoTest[User] {
  override val element: User = User(Some("_id"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  override val updatedElement: User = User(Some("_id"), "name", "familyname2", "test@test.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  override val secondElement: User = User(Some("_id2"), "name", "familyname", "test@test.com", "0600000000", "password", List("machine1"), List("admin"), "compagnyId")
  override val collectionName: String = "users"
  override val dao: AbstractDao[User] = new UserDao()
}
