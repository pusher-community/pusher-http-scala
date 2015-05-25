package com.pusher

import java.net.URI
import Util.checkEmptyCredentials

/**
 *
 * @param appId Pusher application id
 * @param key Pusher application key
 * @param secret Pusher application secret
 * @param ssl If SSL should be used or not
 * @param host Host to connect to
 * @param timeout Request timeout in seconds
 */
class Pusher(val appId: String,
             val key: String,
             val secret: String,
             val ssl: Boolean = true,
             val host: String = "api.pusherapp.com",
             val timeout: Int = 5) {

  self =>

  private var _scheme: String = "http"
  private var _port: Int = 80

  checkEmptyCredentials(Map(
    "appId" -> appId,
    "key" -> key,
    "secret" -> secret
  ))

  if (ssl) {
    _scheme = "https"
    _port = 443
  }

  /**
   * Getter for the scheme
   * @return String
   */
  def scheme = _scheme

  /**
   * Getter for the port number
   * @return Int
   */
  def port = _port

  /**
   * Trigger an event
   * @param channel Channel to trigger the event on
   * @param eventName Name of the event
   * @param data Data to send
   */
  def trigger(channel:String, eventName: String, data: String): Unit = {
    val params: Map[String, String] = Map(
      "name" -> eventName,
      "channel" -> channel,
      "data" -> data
    )

    Request(self, "POST", "/events", params)
  }

  /**
   * Get information for multiple channels
   * @param prefixFilter Prefix to filter channels with
   * @param attributes Attributes to be returned for each channel
   */
  def channelsInfo(prefixFilter: String = null,
                   attributes: List[String] = List()): Unit = {
    var params: Map[String, String] = Map()
    if (attributes.nonEmpty) {
      params += ("info" -> attributes.mkString(","))
    }

    if (prefixFilter.nonEmpty) {
      params += ("filter_by_prefix" -> prefixFilter)
    }

    Request(self, "GET", "/channels", params)
  }

  /**
   * Get info for one channel
   * @param channel Name of channel
   * @param attributes Attributes requested
   */
  def channelInfo(channel: String,
                  attributes: List[String] = List()): Unit = {
    var params: Map[String, String] = Map()
    if (attributes.nonEmpty) {
      params += ("info" -> attributes.mkString(","))
    }

    Request(self, "GET", s"/channels/$channel", params)
  }

  /**
   * Fetch user id's subscribed to a channel
   * @param channel Name of channel
   */
  def usersInfo(channel: String): Unit = {
    Request(self, "GET", s"/channels/$channel/users")
  }
}

object Pusher {
  /**
   * Companion object
   *
   * Instantiate Pusher using a URI
   * @param uri The pusher URI to be parsed
   * @return Pusher
   */
  def fromUrl(uri: String) = {
    val url: URI = new URI(uri)
    val appId: String = url.getPath.split("/")(2)
    val key: String = url.getUserInfo.split(":")(0)
    val secret: String = url.getUserInfo.split(":")(1)
    var ssl: Boolean = false
    var port: Int = 443
    val host: String = url.getHost

    if (url.getScheme == "http") {
      ssl = false
    } else {
      ssl = true
    }

    new Pusher(appId, key, secret, ssl, host)
  }
}


