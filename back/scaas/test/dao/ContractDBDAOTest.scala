package dao

import common.dao.AbstractDao
import smart_contracts.dao.ContractDBDAO
import smart_contracts.model.dao.Contract

class ContractDBDAOTest extends AbstractDaoTest[Contract] {
  override val element: Contract = Contract(Some(id), "Contract.json", "ethereum", "fileContent", "idProject", "idUser")
  override val updatedElement: Contract = Contract(Some(id), "Contract.json", "ethereum", "fileContentz", "idProject", "idUser")
  override val secondElement: Contract = Contract(Some("id2"), "OtherContract.json", "ethereum", "fileContent", "idProject", "idUser")
  override val collectionName: String = "contracts"
  override val dao: AbstractDao[Contract] = new ContractDBDAO()
}

