package common.controllers

import applications.model.dao.Application
import play.api.libs.json.__
import play.api.test.PlaySpecification


class JsonSanitizerTest extends PlaySpecification {


  val application: Application = Application(Some("id"),
    "Dapps 1",
    None,
    "secretHashedApiKey",
    "salt",
    "abf-testnet",
    "walletId"
  )

  "JsonSanitizerTest" should {
    "sanitize a first level JSON object" in {
      val sanitize = JsonSanitizer(__ \ "_id", __ \ "hashedApiKey")("idkey")(Application.format)
      val sanitized = sanitize(application)
      val transformer = (__ \ "hashedApiKey").json.pick
      val transformer2 = (__ \ "_id").json.pick

      sanitized.transform(transformer).isError mustEqual true
      sanitized.transform(transformer2).isError mustEqual true

      ok
    }
  }


}
