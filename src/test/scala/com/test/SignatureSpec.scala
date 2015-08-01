package com.test

import org.scalatest.FunSpec

import com.pusher.Signature._

class SignatureSpec extends FunSpec {
  describe("#sign") {
    it("should sign a string with a secret using HMAC256") {
      val signature = sign("somesecret", "stringtosign")
      assert(signature == "8a945866c155804fcc64c7210c2d4736c03d25275785604046bbff7360a0aa17")
    }
  }

  describe("#verify") {
    it("should verify a signature with the correct secret") {
      val verified = verify(
        "somesecret",
        "stringtosign",
        "8a945866c155804fcc64c7210c2d4736c03d25275785604046bbff7360a0aa17"
      )

      assert(verified)
    }

    it("should fail if the secret is incorrect") {
      val verified = verify(
       "wrongsecret",
       "stringtosign",
       "8a945866c155804fcc64c7210c2d4736c03d25275785604046bbff7360a0aa17"
      )

      assert(!verified)
    }
  }
}
