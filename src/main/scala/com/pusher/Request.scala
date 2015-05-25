package com.pusher

import com.pusher.Signature.sign
import org.json4s.DefaultFormats
import org.json4s.native.Serialization.write
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
              private var _params: Map[String, String] = Map(),
              private var _body: String = null) {

  private var headers: Map[String, String] = Map()
  private var queryParams: Map[String, String] = _params

  implicit val formats = DefaultFormats

  /**
   * Getter for body
   * @return String
   */
  def body = _body

  /**
   * Getter for params
   * @return Map
   */
  def params = _params

  /**
   * Generate authentication for the request
   */
  private def generateAuth(): Unit = {
    if (verb == "POST") {
      _body = write(_params)
      queryParams = Map()
      queryParams += ("body_md5" -> generateMD5Hash(_body).toString)
      headers += ("Content-Type" -> "application/json")
    }

    queryParams += (
      "auth_key" -> client.key,
      "auth_version" -> "1.0",
      "auth_timestamp" -> (System.currentTimeMillis / 1000).toString
    )

    val authString = List(
      verb,
      path,
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
   * Add headers to an HTTP request if they exist
   * @param request HTTPRequest to which headers must be added
   * @return HTTPRequest
   */
  private def addHeaders(request: HttpRequest): HttpRequest = {
    if (headers.nonEmpty) request.headers(headers)

    request
  }

  /**
   * Make a new HTTP request
   * Generate all auth that is required to be sent
   */
  def makeRequest(): HttpResponse[String] = {
    generateAuth()
    val request: HttpRequest =
      addHeaders(Http(endpoint()).method(verb).params(queryParams))
    verb match {
      case "POST" =>
        request.postData(_body).asString
      case "GET" =>
        request.asString
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
            params: Map[String, String] = Map(),
            body: String = null) = {
    new Request(client, verb, path, params, body).makeRequest()
  }
}

