package com.test

import com.pusher.TriggerData
import com.pusher.Util._

import org.scalatest.FunSpec

class UtilSpec extends FunSpec {
  describe("#encodeJson") {
    it("should encode a Map[String, Any] into a String") {
      val encodedJson = encodeJson(Map("blah" -> "test", "test" -> 1))
      assert(encodedJson == "{\"blah\":\"test\",\"test\":1}")
    }
  }

  describe("#encodeTriggerData") {
    it("should encode TriggerData into a JSON string") {
      val triggerData = TriggerData(
        List("test_channel"),
        "test_event",
        "lalalalal",
        None
      )

      assert(encodeTriggerData(triggerData) ==
        "{\"channels\":[\"test_channel\"],\"name\":\"test_event\",\"data\":\"lalalalal\"}"
      )
    }
  }

  describe("#generatedMD5Hash") {
    it("should generate an MD5 hash for a string") {
      assert(generateMD5Hash("testing") == "ae2b1fca515949e5d54fb22b8ed95575")
    }
  }
}
