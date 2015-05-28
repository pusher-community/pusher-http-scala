package com.pusher

import java.net.URI
import Util._
import com.pusher.Types.PusherResponse
import com.pusher.Signature.sign

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

  validateCredentials(Map(
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
   * @param channels Channels to trigger the event on
   * @param eventName Name of the event
   * @param data Data to send
   * @param socketId Socked ID to exclude
   * @return PusherResponse
   */
  def trigger(channels: List[String],
              eventName: String,
              data: String,
              socketId: Option[String]): PusherResponse = {
    validateEventNameLength(eventName)
    validateDataLength(data)

    if (socketId.isDefined) {
      validateSocketId(socketId.get)
    }

    val triggerData: TriggerData =
      TriggerData(channels, eventName, data, socketId)

    Request(self, "POST", "/events", null, encodeTriggerData(triggerData))
  }

  /**
   * Get information for multiple channels
   * @param prefixFilter Prefix to filter channels with
   * @param attributes Attributes to be returned for each channel
   * @return PusherResponse
   */
  def channelsInfo(prefixFilter: Option[String],
                   attributes: Option[List[String]]): PusherResponse = {
    var params: Map[String, String] = Map()
    if (attributes.isDefined) {
      params += ("info" -> attributes.get.mkString(","))
    }

    if (prefixFilter.isDefined) {
      params += ("filter_by_prefix" -> prefixFilter.get)
    }

    Request(self, "GET", "/channels", params)
  }

  /**
   * Get info for one channel
   * @param channel Name of channel
   * @param attributes Attributes requested
   * @return PusherResponse
   */
  def channelInfo(channel: String,
                  attributes: Option[List[String]]): PusherResponse = {
    validateChannel(channel)

    var params: Map[String, String] = Map()
    if (attributes.isDefined) {
      params += ("info" -> attributes.get.mkString(","))
    }

    Request(self, "GET", s"/channels/$channel", params)
  }

  /**
   * Fetch user id's subscribed to a channel
   * @param channel Name of channel
   * @return PusherResponse
   */
  def usersInfo(channel: String): PusherResponse = {
    validateChannel(channel)

    Request(self, "GET", s"/channels/$channel/users")
  }

  /**
   * Generate a delegated client subscription token
   * @param channel Channel to authenticate
   * @param socketId SocketId that required auth
   * @param customData Used on presence channels for info
   * @return String
   */
  def authenticate(channel: String,
                   socketId: String,
                   customData: Option[Map[String, String]]): String = {
    validateChannel(channel)
    validateSocketId(socketId)

    var stringToSign: String = s"$socketId:$channel"
    if (customData.isDefined) {
      val encodedData: String = encodeJson(customData.get)
      stringToSign += s":$encodedData"
    }

    val signature: String = sign(secret, stringToSign)
    val auth: String = s"$key:$signature"
    var result: Map[String, String] = Map(
      "auth" -> auth
    )

    if (customData.isDefined) {
      result += ("channel_data" -> encodeJson(customData.get))
    }

    encodeJson(result)
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
    val host: String = url.getHost

    if (url.getScheme == "http") {
      ssl = false
    } else {
      ssl = true
    }

    new Pusher(appId, key, secret, ssl, host)
  }
}


