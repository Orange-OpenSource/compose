package dao

import common.dao.AbstractDao
import credentials.dao.CredentialDao
import credentials.model.dao.Credential

class CredentialDaoTest extends AbstractDaoTest[Credential] {
  override val element: Credential = Credential(Some(id), "salt", "hashedPassword", Some("opts"))
  override val updatedElement: Credential = Credential(Some(id), "salt", "mot_de_passe", Some("opts"))
  override val secondElement: Credential = Credential(Some("id2"), "salt", "azerty", Some("opts"))
  override val collectionName: String = "credentials"
  override val dao: AbstractDao[Credential] = new CredentialDao()
}
