package com.test

import com.pusher.Pusher
import org.scalatest._

class PusherSpec extends FunSpec {
  describe("Pusher") {
    val pusher = new Pusher("12345", "testkey", "testsecret")

    it("should set the port and scheme to 443 and https if not supplied") {
      assert(pusher.scheme == "https")
      assert(pusher.port == 443)
    }

    it("should set port and scheme to 80 and http if ssl is diasbled") {
      val pusher = new Pusher("12345", "testkey", "testsecret", false)

      assert(pusher.scheme == "http")
      assert(pusher.port == 80)
    }

    describe("validation") {
      it ("should throw an Exception if the key, secret or appId are empty") {
        intercept[Exception] {
          val fakePusher = new Pusher("", "", "")
        }
      }
    }

    describe(".fromUrl") {
      val pusher = Pusher.fromUrl("http://mykey:mysecret@api.pusher.com/apps/432")

      it("should infer all the arguments from the uri") {
        assert(pusher.appId == "432")
        assert(pusher.secret == "mysecret")
        assert(pusher.key == "mykey")
        assert(pusher.scheme == "http")
        assert(pusher.port == 80)
      }
    }
  }
}
