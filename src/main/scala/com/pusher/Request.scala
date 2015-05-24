package com.pusher

import com.pusher.Signature.sign

import dispatch._
import java.net.URI
import java.security.MessageDigest
import java.sql.Timestamp

/**
 * Class to handle HTTP requests
 * @param client A Pusher instance
 * @param verb Type of request to make
 * @param uri The URI to make the request to
 * @param _params Parameters to be passed to the request
 * @param _body Body content of the request
 */
class Request(client: Pusher,
              verb: String,
              uri: String,
              private var _params: Map[String, String],
              private var _body: String) {

  private var headers: Map[String, String] = Map()

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
    if (verb == "POST" && !_body.isEmpty) {
      _params += ("body_md5" -> generateMD5Hash(_body).toString)
    }

    _params += (
      "auth_key" -> client.key,
      "auth_version" -> "1.0",
      "auth_timestamp" -> new Timestamp(System.currentTimeMillis).toString
    )

    val authString = List(
      verb,
      serializedUri.getPath,
      generateQueryString()
    ).mkString("\n")

    _params += ("auth_signature" -> sign(client.secret, authString))
  }

  /**
   * Generate a query string using the parameters
   * @return String
   */
  private def generateQueryString(): String = {
    _params.map { case(k, v) =>
      k + "=" + v.headOption.getOrElse("")
    }.mkString("?", "&", "")
  }

  /**
   * Generate an MD5 hash using the body
   * @param s String for which the hash is to be generated
   * @return String
   */
  private def generateMD5Hash(s: String): String = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).toString
  }

  /**
   * Serialize the uri string to an URI object
   * @return URI
   */
  private def serializedUri: URI = {
    new URI(uri)
  }
}
