package com.test

import com.pusher.PusherConfig
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
    val config = PusherConfig("5","key","secret", ssl = false, Some("eu"), Some(8080))

    it("should set ssl to false") {
      assert(config.ssl === false)
    }

    it("should return the correct scheme") {
      assert(config.scheme === "http")
    }

    it("should return the right port") {
      assert(config.getPort === 8080)
    }

    it("should set the cluster") {
      assert(config.cluster.get === "eu")
    }

    it("should use return the right host") {
      assert(config.getHost === "api-eu.pusher.com")
    }
  }
}
