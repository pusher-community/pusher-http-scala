package com.test

import com.pusher._
import org.scalatest.FunSpec

class PusherSpec extends FunSpec {
  val key = "somekey"
  val secret = "somesecret"
  val pusher = new Pusher("4", key, secret)

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
