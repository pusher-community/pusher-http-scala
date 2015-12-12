package com.test

import com.pusher.Util._
import com.pusher._
import org.scalatest.FunSpec
import org.specs2.mock.Mockito

class PusherSpec extends FunSpec with Mockito {
  val appId = "4"
  val key = "somekey"
  val secret = "somesecret"
  val pusher = new Pusher("4", key, secret)

  val pusherSpy = spy(new Pusher(appId, key, secret))

  describe("#trigger") {
    it("should trigger an event and return TriggerResponse") {
      val channels = List("chan1", "chan2")
      val eventName = "blah"
      val data = "tada"
      val triggerData = TriggerData(channels, eventName, data, None)
      val requestParams = RequestParams(
        PusherConfig(appId, key, secret),
        "POST",
        "/events",
        None,
        Some(encodeTriggerData(triggerData))
      )

      class MockedRequest extends Request(requestParams)
      val mockedRequest = mock[MockedRequest]
      pusherSpy.requestObject(requestParams) returns mockedRequest
      mockedRequest.rawResponse() returns Right("{}")

      val response = pusherSpy.trigger(channels, eventName, data, None)

      there was one(pusherSpy).trigger(channels, eventName, data, None)
      assert(response == Right(TriggerResponse()))
    }

    it("should return a PusherError if validation fails for any reason") {
      val response = pusher.trigger(List("ivc:", "ivc2:"), "meh", "lala")
      assert(response == Left(ValidationError("Invalid channel name")))
    }
  }

  describe("#channelsInfo") {
    it("should query pusher for info about multiple channels and return ChannelsInfoResponse") {
      val requestParams = RequestParams(
        PusherConfig(appId, key, secret),
        "GET",
        "/channels",
        Some(Map.empty[String, String]),
        None
      )

      class MockedRequest extends Request(requestParams)
      val mockedRequest = mock[MockedRequest]
      pusherSpy.requestObject(requestParams) returns mockedRequest
      mockedRequest.rawResponse() returns Right("{\"channels\":{\"presence-foobar\":{}}}")

      val response = pusherSpy.channelsInfo(None, None)

      there was one(pusherSpy).channelsInfo(None, None)
      assert(response == Right(ChannelsInfoResponse(Map("presence-foobar" -> ChannelDetails(None)))))

    }
  }

  describe("#channelInfo") {
    it("should query pusher for info about a single channel and return a ChannelInfoResponse") {
      val requestParams = RequestParams(
        PusherConfig(appId, key, secret),
        "GET",
        "/channels/test-channel",
        Some(Map.empty[String, String]),
        None
      )

      class MockedRequest extends Request(requestParams)
      val mockedRequest = mock[MockedRequest]
      pusherSpy.requestObject(requestParams) returns mockedRequest
      mockedRequest.rawResponse() returns Right("{\"occupied\": true }")

      val response = pusherSpy.channelInfo("test-channel", None)
      assert(response == Right(ChannelInfoResponse(occupied = true, None, None)))
    }
  }

  describe("#usersInfo") {
    it("should return user ids associated with a presence channel") {
      val requestParams = RequestParams(
        PusherConfig(appId, key, secret),
        "GET",
        "/channels/presence-test-channel/users",
        None,
        None
      )

      class MockedRequest extends Request(requestParams)
      val mockedRequest = mock[MockedRequest]
      pusherSpy.requestObject(requestParams) returns mockedRequest
      mockedRequest.rawResponse() returns Right("{\"users\": [{\"id\": \"1\"}]}")

      val response = pusherSpy.usersInfo("presence-test-channel")
      assert(response == Right(UsersInfoResponse(List(UserDetails("1")))))
    }
  }

  describe("#authenticate") {
    it("should authenticate private channels") {
      val auth = pusher.authenticate("private-test-channel", "12376.123", None)

      assert(auth == "{\"auth\":\"somekey:81a249f5aae14a7ffdef6574bc7aceeac10472f79e1b74283d3306d4a513bb89\"}")
    }

    it("should authenticate presence channels") {
      val auth = pusher.authenticate(
        "presence-channel",
        "12356.342",
        Some(PresenceUser("123", Map("key" -> "value")))
      )
      assert(
        auth
        ==
        "{\"auth\":\"somekey:1075d43b7deaa9b1c7ccf6f28cb035d858ca25e27f5c140d9841ee418909ad76\"," +
          "\"channel_data\":\"{\\\"user_id\\\":\\\"123\\\",\\\"user_info\\\":{\\\"key\\\":\\\"value\\\"}}\"}"
      )
    }
  }

  describe("#validateWebhook") {
    val body = "{\"time_ms\": 1327078148132,\"events\": []}"
    val signature = Signature.sign(secret, body)

    it("should validate genuine webhooks") {
      val webhookBody = pusher.validateWebhook(key, signature, body)
      assert(webhookBody == Right(WebhookResponse(1327078148132L, List())))
    }

    it("should return a WebhookError if the key's do not match") {
      val webhookBody = pusher.validateWebhook("blah", signature, body)
      assert(webhookBody == Left(WebhookError("Key's did not match when verifying webhook")))
    }

    it("should return a WebhookError if the signatures do not match") {
      val webhookBody = pusher.validateWebhook(key, "faksignature", body)
      assert(webhookBody == Left(WebhookError("Received webhook with invalid signature")))
    }

    it("should return a WebhookError if the webhook is too old") {
      val invalidBody = "{\"time_ms\": 1000000}"
      val fakeSignature = Signature.sign(secret, invalidBody)
      val webhookBody = pusher.validateWebhook(key, fakeSignature, invalidBody)
      assert(webhookBody == Left(WebhookError("Webhook time not within 300 seconds")))
    }
  }
}
