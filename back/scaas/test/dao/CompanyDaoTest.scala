package dao

import common.dao.AbstractDao
import companies.dao.CompanyDao
import companies.model.dao.Company

class CompanyDaoTest extends AbstractDaoTest[Company] {

    override val collectionName: String = "companies"
    override val element: Company = Company(Some(id),
        List("0123456789465", "123465789465"))
    override val updatedElement: Company = Company(Some(id),
        List("0123456789465", "123465789465"))
    override val secondElement: Company = Company(Some("idSecond"),
        List("0123456789465", "123465789465"))
    override val dao: AbstractDao[Company] = new CompanyDao()
}
