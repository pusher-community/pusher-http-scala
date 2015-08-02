package com.test

import com.pusher.Request.parseResponse
import com.pusher._

import org.scalatest.FunSpec

class RequestSpec extends FunSpec {
  describe("#parseResponse") {
    it("should deserialise trigger JSON into TriggerResponse") {
      assert(parseResponse[TriggerResponse]("{}") == Right(TriggerResponse()))
    }

    it("should deserialise channel info JSON into ChannelInfoResponse") {
      assert(parseResponse[ChannelInfoResponse](
        "{\"occupied\":true,\"user_count\":42,\"subscription_count\":42}")
        == Right(ChannelInfoResponse(occupied = true, Some(42), Some(42)))
      )
    }

    it("should deserialise users info JSON into UsersInfoResponse") {
      assert(parseResponse[UsersInfoResponse](
        "{\"users\":[{\"id\":\"1\"},{\"id\":\"2\"}]}")
        == Right(UsersInfoResponse(List(UserDetails("1"), UserDetails("2"))))
      )
    }

    it("should fail if there JSON is invalid") {
      assert(parseResponse[TriggerResponse]("test") == Left(JSONParsingError("Failed to parse JSON: test")))
    }
  }

}
