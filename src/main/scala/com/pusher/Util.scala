package com.pusher

import java.security.MessageDigest

import org.json4s.DefaultFormats
import org.json4s.JsonDSL._

import scala.util.matching.Regex
import org.json4s.native.Serialization.{write, read}

object Util {

  implicit val formats = DefaultFormats

  /**
   * Check if event name is valid
   * @param eventName Event name to be validated
   * @return Option[ValidationError]
   */
  def validateEventName(eventName: String): Option[ValidationError] = {
    if (eventName.length > 200) {
      Some(ValidationError("Event name is too long"))
    } else None
  }

  /**
   * Check if the data size is under the allowable limit
   * @param data Data string to be validated
   * @return Option[ValidationError]
   */
  def validateDataLength(data: String): Option[ValidationError] = {
    if (data.getBytes.length > 10240) {
      Some(ValidationError("Data size is above 10240 bytes"))
    } else None
  }

  /**
   * Check if socket id matches the correct pattern
   * @param socketId Socket id to be validated
   * @return Option[ValidationError]
   */
  def validateSocketId(socketId: String): Option[ValidationError] = {
    val regex: Regex = new Regex("\\A\\d+\\.\\d+\\z")
    if (!regex.pattern.matcher(socketId).matches()) {
      Some(ValidationError("Invalid socket id"))
    } else None
  }

  /**
   * Check if channel names are valid
   * @param channel Channel to be checked
   * @return Option[ValidationError]
   */
  def validateChannel(channel: String): Option[ValidationError] = {
    val regex: Regex = new Regex("\\A[-a-zA-Z0-9_=@,.;]+\\z")
    if (!regex.pattern.matcher(channel).matches()) {
      Some(ValidationError("Invalid channel name"))
    } else None
  }

  /**
   * Validate number of channels when triggering.
   * @param channels List of channels
   * @return Option[ValidationError]
   */
  def validateChannelCount(channels: List[String]): Option[ValidationError] = {
    if (channels.length > 10) {
      Some(ValidationError("Max 10 channels allowed"))
    } else None
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
   * Decode a JSON string into a Map
   * @param data String to be decoded
   * @return Map[String, Any]
   */
  def decodeJson(data: String): Map[String, Any] = {
    read(data)
  }

  /**
   * Encode TriggerData into JSON
   * @param triggerData Data to be encoded
   * @return String
   */
  def encodeTriggerData(triggerData: TriggerData): String = {
    val json =
      ("channels" -> triggerData.channels) ~
      ("name" -> triggerData.eventName) ~
      ("data" -> triggerData.data) ~
      ("socket_id" -> triggerData.socketId)

    write(json)
  }

  /**
   * Generate an MD5 hash using the body
   * @param s String for which the hash is to be generated
   * @return String
   */
   def generateMD5Hash(s: String): String = {
    MessageDigest.getInstance("MD5").digest(s.getBytes).map("%02x".format(_)).mkString
  }
}


