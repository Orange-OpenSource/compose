package dao

import common.dao.AbstractDao
import smart_contracts.dao.AbiDao
import smart_contracts.model.dao.Abi

class AbiDaoTest extends AbstractDaoTest[Abi] {

  override val collectionName: String = "abis"
  override val element: Abi = Abi(Some(id), "my_abi", "abf-testnet", "lol", "0x1234567894", "userID")
  override val updatedElement: Abi = Abi(Some(id), "my_abi", "abf-testnet", "lol", "0x1234567445", "userID")
  override val secondElement: Abi = Abi(Some("_id2"), "my_abi2", "abf-testnet", "lol", "0x1234563894", "userID")
  override val dao: AbstractDao[Abi] = new AbiDao()
}
