package com.test

import com.pusher._
import org.scalatest.FunSpec
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import scala.util.{Success, Failure}
import scalaj.http.{HttpRequest, HttpResponse}

class RequestSpec extends FunSpec with MockitoSugar {
  describe("#buildRequest") {
    describe("GET request") {
      it("should correctly build the request") {
        val pusherConfig = PusherConfig("123", "key", "secret")
        val requestParams = RequestParams(
          pusherConfig,
          "GET",
          "/channels/test-channel",
          None,
          None
        )

        val request = new Request(requestParams)
        val httpRequest = request.buildRequest()

        assert(httpRequest.method == "GET")
        assert(httpRequest.url == "https://api.pusherapp.com:443/apps/123/channels/test-channel")
      }
    }

    describe("POST request") {
      it("should correctly build the request") {
        val pusherConfig = PusherConfig("123", "key", "secret")
        val requestParams = RequestParams(
          pusherConfig,
          "POST",
          "/events",
          None,
          Some("data")
        )

        val request = new Request(requestParams)
        val httpRequest = request.buildRequest()

        assert(httpRequest.method == "POST")
        assert(httpRequest.url == "https://api.pusherapp.com:443/apps/123/events")
      }
    }

    describe("query params") {
      it("should correctly add the mandatory query params") {
        val pusherConfig = PusherConfig("123", "key", "secret")
        val requestParams = RequestParams(
          pusherConfig,
          "GET",
          "/events",
          None,
          None
        )

        val request = new Request(requestParams)
        val httpRequest = request.buildRequest()
        val expectedKeys = Set(
          "auth_key",
          "auth_version",
          "auth_timestamp",
          "auth_signature"
        )
        assert(httpRequest.params.toMap.keys == expectedKeys)
      }

      it("should add optional query params") {
        val pusherConfig = PusherConfig("123", "key", "secret")
        val requestParams = RequestParams(
          pusherConfig,
          "GET",
          "/events",
          Some(Map("key" -> "value")),
          None
        )

        val request = new Request(requestParams)
        val httpRequest = request.buildRequest()
        val expectedKeys = Set(
          "auth_key",
          "key",
          "auth_version",
          "auth_timestamp",
          "auth_signature"
        )
        assert(httpRequest.params.toMap.keys == expectedKeys)
      }
    }
  }

  describe("#handleResponse") {
    it("should return the response body if the HTTP code is 200") {
      val mockResponse = Success(HttpResponse[String]("tada", 200, Map()))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Right("tada"))
    }

    it("should return PusherBadRequestError if the HTTP code is 400") {
      val mockResponse = Success(HttpResponse[String]("Whoopsie!", 400, Map()))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Left(PusherBadRequestError("Whoopsie!")))
    }

    it("should return PusherBadAuthError if the HTTP code is 401") {
      val mockResponse = Success(HttpResponse[String]("Nooooop!", 401, Map()))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Left(PusherBadAuthError("Nooooop!")))
    }

    it("should return PusherForbiddenError if the HTTP code is 403") {
      val mockResponse = Success(HttpResponse[String]("Sorry bruh!", 403, Map()))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Left(PusherForbiddenError("Sorry bruh!")))
    }

    it("should return PusherBadStatusError if the HTTP code is anything else") {
      val mockResponse = Success(HttpResponse[String]("Uh oh!", 500, Map()))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Left(PusherBadStatusError("Uh oh!")))
    }

    it("should return PusherRequestFailed error if the request fails") {
      val mockResponse = Failure(new Exception("HTTP boo!"))
      val resp = Request.handleResponse(mockResponse)

      assert(resp == Left(PusherRequestFailedError("HTTP boo!")))
    }
  }
}
