package dao

import common.dao.AbstractDao
import login.dao.TokensDao
import login.model.Token

class TokensDaoTest extends AbstractDaoTest[Token] {
  override val element: Token = Token(Some(id),id, "76e42a99dbf92380b9dfc441fd26180f7e048e1bd9e7386b", 1705489353651L )
  override val updatedElement: Token = Token(Some(id),id, "87f8ebdc2b21f87fc8db60a3f21c2868bd7b21a430892c94", 1702552935112L )
  override val secondElement: Token = Token(Some("_id2"),"_id", "55d0ff878b3734a92da8943f2d429c4952eea14dd69d25b2", 1705489353651L )
  override val collectionName: String = "tokens"
  override val dao: AbstractDao[Token] = new TokensDao()
}
