package com.pusher

import com.pusher.Signature.sign
import com.pusher.Types.PusherResponse

import java.net.URI
import org.json4s.native.JsonMethods.parse
import org.json4s.DefaultFormats
import java.security.MessageDigest
import scalaj.http._

/**
 * Class to handle HTTP requests
 * @param client A Pusher instance
 * @param verb Type of request to make
 * @param path The path to make the request to
 * @param _params Parameters to be passed to the request
 * @param _body Optional body
 */
class Request(client: Pusher,
              verb: String,
              path: String,
              private var _params: Option[Map[String, String]],
              private var _body: Option[String]) {
  private var queryParams: Map[String, String] = _params.getOrElse(Map())

  implicit val formats = DefaultFormats

  /**
   * Getter for body
   * @return String
   */
  def body = _body.getOrElse("")

  /**
   * Getter for params
   * @return Map
   */
  def params = _params.getOrElse(Map())

  /**
   * Generate authentication for the request
   */
  private def generateAuth(): Unit = {
    if (verb == "POST") {
      queryParams = Map()

      if (_body.isDefined) {
        queryParams += ("body_md5" -> generateMD5Hash(_body.get).toString)
      }
    }

    queryParams += (
      "auth_key" -> client.key,
      "auth_version" -> "1.0",
      "auth_timestamp" -> (System.currentTimeMillis / 1000).toString
    )

    val authString = List(
      verb,
      new URI(endpoint()).getPath,
      generateQueryString()
    ).mkString("\n")
    
    queryParams += ("auth_signature" -> sign(client.secret, authString))
  }

  /**
   * Generate a query string using the parameters
   * @return String
   */
  private def generateQueryString(): String = {
    queryParams.toSeq.sortBy(_._1).map { case(k, v) =>
      k + "=" + v
    }.mkString("&")
  }

  /**
   * Generate an MD5 hash using the body
   * @param s String for which the hash is to be generated
   * @return String
   */
  private def generateMD5Hash(s: String): String = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }

  /**
   * Get the pusher endpoint
   * @return String
   */
  private def endpoint(): String = {
    val appId = client.appId
    client.scheme + "://" + client.host + s"/apps/$appId" + path
  }

  /**
   * Handle HTTP responses
   * @param response The HTTP response object
   * @return PusherResponse
   */
  private def handleResponse(response: HttpResponse[String]): PusherResponse = {
    val responseBody: String = response.body

    response.code match {
      case 200 => Right(parse(responseBody).extract[Map[String, Any]])
      case 400 => Left(new PusherBadRequestException(responseBody))
      case 401 => Left(new PusherBadAuthException(responseBody))
      case 403 => Left(new PusherForbiddenException(responseBody))
      case _ =>
        val statusCode: Int = response.code
        Left(new PusherBadStatusException(s"$statusCode: $responseBody"))
    }
  }

  /**
   * Make a new HTTP request
   * Generate all auth that is required to be sent
   */
  def makeRequest(): PusherResponse = {
    generateAuth()
    val request: HttpRequest =
      Http(endpoint()).method(verb).params(queryParams)

    verb match {
      case "POST" =>
        val postRequest: HttpRequest =
          request.postData(_body.getOrElse("")).header("Content-Type", "application/json")
        handleResponse(postRequest.asString)
      case "GET" =>
        handleResponse(request.asString)
    }
  }
}

/**
 * Companion object for the Request class
 * Acts as a Singleton
 */
object Request {
  /**
   * Make a new Request
   * @param client  A Pusher instance
   * @param verb Type of request to make
   * @param path The path to make the request to
   * @param params Parameters to be passed to the request
   * @param body Optional body
   * @return Request
   */
  def apply(client: Pusher,
            verb: String,
            path: String,
            params: Option[Map[String, String]],
            body: Option[String]) = {
    new Request(client, verb, path, params, body).makeRequest()
  }
}
