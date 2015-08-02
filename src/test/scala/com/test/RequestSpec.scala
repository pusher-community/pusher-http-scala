package com.test

import com.pusher.Request.parseResponse
import com.pusher._

import org.scalatest.FunSpec

class RequestSpec extends FunSpec {
  describe("#parseResponse") {
    it("should deserialise trigger JSON into TriggerResponse") {
      assert(parseResponse[TriggerResponse]("{}") == Right(TriggerResponse()))
    }

    it("should deserialise channels info JSON into ChannelsInfoResponse with user_count") {
      assert(parseResponse[ChannelsInfoResponse](
        "{\"channels\":{\"presence-foobar\":{\"user_count\":42},\"presence-another\":{\"user_count\":123}}}")
        ==
        Right(
          ChannelsInfoResponse(
            Map("presence-foobar" -> ChannelDetails(Some(42)), "presence-another" -> ChannelDetails(Some(123)))
          )
        )
      )
    }

    it("should deserialise channels info JSON into ChannelsInfoResponse without user_count") {
      assert(parseResponse[ChannelsInfoResponse](
        "{\"channels\":{\"presence-foobar\":{},\"presence-another\":{}}}")
        ==
        Right(
          ChannelsInfoResponse(
            Map("presence-foobar" -> ChannelDetails(None), "presence-another" -> ChannelDetails(None))
          )
        )
      )
    }

    it("should deserialise channel info JSON into ChannelInfoResponse with all attributes") {
      assert(parseResponse[ChannelInfoResponse](
        "{\"occupied\":true,\"user_count\":42,\"subscription_count\":42}")
        ==
        Right(
          ChannelInfoResponse(
            occupied = true,
            Some(42),
            Some(42)
          )
        )
      )
    }

    it("should deserialise channel info JSON into ChannelInfoResponse with missing attributes") {
      assert(parseResponse[ChannelInfoResponse](
        "{\"occupied\":true}")
        ==
        Right(
          ChannelInfoResponse(
            occupied = true,
            None,
            None
          )
        )
      )
    }

    it("should deserialise users info JSON into UsersInfoResponse") {
      assert(parseResponse[UsersInfoResponse](
        "{\"users\":[{\"id\":\"1\"},{\"id\":\"2\"}]}")
        ==
        Right(UsersInfoResponse(
          List(UserDetails("1"), UserDetails("2")))
        )
      )
    }

    it("should fail if there JSON is invalid") {
      assert(parseResponse[TriggerResponse]("test") == Left(JSONParsingError("Failed to parse JSON: test")))
    }
  }
}
