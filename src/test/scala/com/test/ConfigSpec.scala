package com.test

import com.pusher.{PusherConfig, Pusher}
import org.scalatest._

class ConfigSpec extends FunSpec {
  describe("creating config using required arguments") {
    val config = PusherConfig("5","key","secret")
    it("should be constructable") {
      assert(config.appId === "5")
      assert(config.key === "key")
      assert(config.secret === "secret")
    }

    it("should set ssl to true when not specified") {
      assert(config.ssl === true)
    }

    it("should return the right scheme based on ssl") {
      assert(config.scheme === "https")
    }

    it("should return the right port when not specified") {
      assert(config.getPort == 443)
    }

    it("should return the right host when not specified") {
      assert(config.getHost === "api.pusherapp.com")
    }
  }

  describe("creating a config by overriding default values") {
    
  }
}
