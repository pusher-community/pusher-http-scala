package com.test

import com.pusher.Util._
import com.pusher._
import org.scalatest.FunSpec
import org.scalamock.scalatest.MockFactory

import scalaj.http.HttpRequest

class PusherSpec extends FunSpec with MockFactory {
  val key = "somekey"
  val secret = "somesecret"
  implicit val pusherConfig: PusherConfig = PusherConfig("4", key, secret)

  describe("#authenticate") {
    it("should authenticate private channels") {
      val auth = Pusher.authenticate("private-test-channel", "12376.123", None)

      assert(auth == "{\"auth\":\"somekey:81a249f5aae14a7ffdef6574bc7aceeac10472f79e1b74283d3306d4a513bb89\"}")
    }

    it("should authenticate presence channels") {
      val auth = Pusher.authenticate(
        "presence-channel",
        "12356.342",
        Some(Map("user_id" -> 123, "user_info" -> Map("key" -> "value")))
      )
      assert(
        auth
        ==
        "{\"auth\":\"somekey:d94ac8df09a2c2de1d9c53100194a73ec1e8d96bb2cf37d99e5910c7c89a0056\"," +
          "\"channel_data\":\"{\\\"user_id\\\":123,\\\"user_info\\\":{\\\"key\\\":\\\"value\\\"}}\"}"
      )
    }
  }

  describe("#validateWebhook") {
    val currentTime = System.currentTimeMillis / 1000
    val body = Util.encodeJson(Map("time_ms" -> currentTime))
    val signature = Signature.sign(secret, body)

    it("should validate genuine webhooks") {
      val webhookBody = Pusher.validateWebhook(key, signature, body)
      assert(webhookBody == Right(WebhookResponse(currentTime, List())))
    }

    it("should return a WebhookError if the key's do not match") {
      val webhookBody = Pusher.validateWebhook("blah", signature, body)
      assert(webhookBody == Left(WebhookError("Key's did not match when verifying webhook")))
    }

    it("should return a WebhookError if the signatures do not match") {
      val webhookBody = Pusher.validateWebhook(key, "faksignature", body)
      assert(webhookBody == Left(WebhookError("Received webhook with invalid signature")))
    }

    it("should return a WebhookError if the webhook is too old") {
      val invalidBody = "{\"time_ms\": 1000000}"
      val fakeSignature = Signature.sign(secret, invalidBody)
      val webhookBody = Pusher.validateWebhook(key, fakeSignature, invalidBody)
      assert(webhookBody == Left(WebhookError("Webhook time not within 300 seconds")))
    }
  }
}
