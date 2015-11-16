package com.test

import com.pusher.Request.parseResponse
import com.pusher._

import org.scalatest.FunSpec

class ParsingSpec extends FunSpec {
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

    it("should deserialise webhook channel existence responses into WebhookResponse") {
      assert(parseResponse[WebhookResponse]
        ("{\"time_ms\": 1327078148132,\"events\": [{ \"name\": \"channel_occupied\", " +
          "\"channel\": \"test_channel\"}]}")
        ==
        Right(
          WebhookResponse(
            1327078148132L,
            List(
              WebhookEvent(
                "channel_occupied",
                "test_channel",
                None,
                None,
                None,
                None
              )
            )
          )
        )
      )
    }

    it("should deserialise webhook presence events into WebhookResponse") {
      assert(parseResponse[WebhookResponse]
        ("{\"time_ms\": 1327078148132,\"events\": [{\"name\": \"member_added\", " +
          "\"channel\": \"presence-your_channel_name\", \"user_id\": \"123\"}]}")
        ==
        Right(
          WebhookResponse(
            1327078148132L,
            List(
              WebhookEvent(
                "member_added",
                "presence-your_channel_name",
                None,
                None,
                None,
                Some("123")
              )
            )
          )
        )
      )
    }

    it("should deserialise webhook client events into WebhookResponse") {
      assert(parseResponse[WebhookResponse]
        ("{\"time_ms\": 1327078148132,\"events\": [{\"name\":\"client_event\"," +
          "\"channel\":\"chan\",\"event\":\"event\"," +
          "\"data\":\"data\",\"socket_id\":\"socket_id\"," +
          "\"user_id\":\"user_id\"}]}")
        ==
        Right(
          WebhookResponse(
            1327078148132L,
            List(
              WebhookEvent(
                "client_event",
                "chan",
                Some("event"),
                Some("data"),
                Some("socket_id"),
                Some("user_id")
              )
            )
          )
        )
      )
    }
  }
}
