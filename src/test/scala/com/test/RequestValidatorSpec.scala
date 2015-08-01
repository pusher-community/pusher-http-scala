package com.test

import com.pusher.ValidationError
import org.scalatest.FunSpec

import com.pusher.RequestValidator._

import scala.util.Random

class RequestValidatorSpec extends FunSpec {
  describe("#validateEventName") {
    it("should succeed if event name length is < 200") {
      assert(validateEventName("valid_event_name").isEmpty)
    }

    it("should return a ValidationError if the event name length is more than 200") {
      assert(validateEventName(Random.alphanumeric.take(220).mkString)
        .contains(ValidationError("Event name is too long"))
      )
    }
  }

  describe("#validateDataLength") {
    it("should succeed if the data length is < 10240") {
      assert(validateDataLength("validdatamuhhahahaha").isEmpty)
    }

    it("should return a ValidationError if the data length is above 10240 bytes") {
      assert(validateDataLength(Random.alphanumeric.take(10245).mkString)
        .contains(ValidationError("Data size is above 10240 bytes"))
      )
    }
  }

  describe("#validateSocketId") {
    it("should validate the socket id against a regex") {
      assert(validateSocketId("1237123.112341").isEmpty)
    }

    it("should fail when there is a trailing colon") {
      assert(validateSocketId("1.1:").contains(ValidationError("Invalid socket id")))
    }

    it("should fail when there is a leading colon") {
      assert(validateSocketId(":1.1").contains(ValidationError("Invalid socket id")))
    }

    it("should fail when there is a leading colon with a new line") {
      assert(validateSocketId(":\n1.1").contains(ValidationError("Invalid socket id")))
    }

    it("should fail when there is a trailing colon with a new line") {
      assert(validateSocketId("1.1\n:").contains(ValidationError("Invalid socket id")))
    }
  }

  describe("#validateChannel") {
    it("should validate the channel name against a regex") {
      assert(validateChannel("valid_chanel").isEmpty)
    }

    it("should succeed with a complex channel name") {
      assert(validateChannel("-azAZ9_=@,.;").isEmpty)
    }

    it("should fail when there is a trailing colon") {
      assert(validateChannel("invalid_channel:").contains(ValidationError("Invalid channel name")))
    }

    it("should fail when there is a leading colon") {
      assert(validateChannel(":invalid_channel:").contains(ValidationError("Invalid channel name")))
    }

    it("should fail when there is a leading colon with a new line") {
      assert(validateChannel(":\ninvalid_channel").contains(ValidationError("Invalid channel name")))
    }

    it("should fail when there is a trailing colon with a new line") {
      assert(validateChannel("invalid_channel\n:").contains(ValidationError("Invalid channel name")))
    }

  }

  describe("#validateChannelCount") {
    it("should succeed if the number of channels is < 10") {
      assert(validateChannelCount(List("one_channel", "one_more")).isEmpty)
    }

    it("should fail if channel count exceeds 10") {
      assert(
        validateChannelCount(
          List(
            "chan_1", "chan_2", "chan_3", "chan_4", "chan_5", "chan_6",
            "chan_7", "chan_8", "chan_9", "chan_10", "chan_11", "chan_12"
          )
        ).contains(ValidationError("Max 10 channels allowed"))
      )
    }
  }
}

