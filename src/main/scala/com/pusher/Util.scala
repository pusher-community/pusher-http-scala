package com.pusher

import org.json4s.DefaultFormats

import scala.util.matching.Regex
import org.json4s.native.Serialization.{write, read}

object Util {

  implicit val formats = DefaultFormats

  /**
   * Validate required params for Pusher
   * @param credentialMap Map containing pusher credentials
   */
  def validateCredentials(credentialMap: Map[String, String]): Unit = {
    credentialMap.foreach {
      case(k, v) =>
        if (v.isEmpty) throw new Exception("Empty credential " + k)
    }
  }

  /**
   * Checks if an event name is too long
   * @param eventName Name of the event
   */
  def validateEventNameLength(eventName: String): Unit = {
    if (eventName.length > 200) throw new Exception("Event name is too long")
  }

  /**
   * Check if data size is too large
   * @param data Data string to be checked for
   */
  def validateDataLength(data: String): Unit = {
    if (data.length > 10240) throw new Exception("Data is too big")
  }

  /**
   * Check if socket id is valid
   * @param socketId Socket id to be validated
   * @return String
   */
  def validateSocketId(socketId: String): Unit = {
    val regex: Regex = new Regex("\\A\\d+\\.\\d+\\z")
    if (!regex.pattern.matcher(socketId).matches()) {
      throw new Exception("Invalid socket id: " + socketId)
    }
  }

  /**
   * Validate a channel
   * @param channel Channel to be validated
   * @return String
   */
  def validateChannel(channel: String): Unit = {
    if (channel.length > 200) throw new Exception("Channel name is too long " + channel)

    val regex: Regex = new Regex("\\A[-a-zA-Z0-9_=@,.;]+\\z")
    if (!regex.pattern.matcher(channel).matches()) {
      throw new Exception("Invalid channel name " + channel)
    }
  }

  /**
   * Encode Map into a JSON string
   * @param data Map to be encoded
   * @return String
   */
  def encodeJson(data: Map[String, Any]): String = {
    write(data)
  }

  /**
   * Decode JSON into a Map
   * @param data Data to decode
   * @return
   */
  def decodeJson(data: String): Map[String, Any] = {
    read(data)
  }
}
