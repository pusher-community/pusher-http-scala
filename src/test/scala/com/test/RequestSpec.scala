package com.test

import com.pusher._
import org.scalatest.FunSpec
import org.mockito.Mockito.when
import org.scalatest.mock.MockitoSugar

import scala.util.{Success, Failure}
import scalaj.http.HttpResponse

class RequestSpec extends FunSpec with MockitoSugar {
  describe("#makeRequest") {
    describe("when making a GET request") {
      it("should return RawPusherResponse") {
        val pusherConfig = PusherConfig("id", "key", "secret")
        val requestParams = RequestParams(
          pusherConfig,
          "GET",
          "/channels/test-channel",
          None,
          None
        )

        class MockRequest extends Request(requestParams)
        val mockRequest = mock[MockRequest]
        val body = "{\"occupied\": true,\"user_count\": 42,\"subscription_count\": 42}"

        when(mockRequest.makeRequest()).thenReturn(Right(body))

        assert(mockRequest.makeRequest() == Right(body))
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
