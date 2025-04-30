package utils

import common.utils.Hex
import play.api.test.PlaySpecification
import scala.collection.immutable

class HexTest extends PlaySpecification {

  "HexTest" should {
    "convert to an hexadecimal string" in {
      val input = Array[Byte](0xa5.toByte, 0xb2.toByte, 0x54.toByte, 0x11.toByte, 0.toByte, 0xf5.toByte)
      Hex().toHexString(immutable.ArraySeq.unsafeWrapArray(input)) mustEqual "a5b2541100f5"
    }

  }

}
