package com.pusher

import java.security.MessageDigest

import org.json4s.DefaultFormats
import org.json4s.JsonDSL._

import org.json4s.native.Serialization.write

object Util {

  implicit val formats = DefaultFormats

  /**
   * Encode Map into a JSON string
   * @param data Map to be encoded
   * @return String
   */
  def encodeJson(data: Map[String, Any]): String = {
    write(data)
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
