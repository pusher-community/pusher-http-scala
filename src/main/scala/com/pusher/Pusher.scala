package com.pusher

import com.pusher.RequestValidator.{
  validateChannel,
  validateEventName,
  validateChannelCount,
  validateDataLength
}

import com.pusher.Util.{
  encodeTriggerData,
  encodeJson,
  decodeJson
}
import com.pusher.Types.PusherResponse
import com.pusher.Signature.{sign, verify}

/**
 * Pusher object
 */
object Pusher {

  /**
   * Trigger an event
   * @param pusherConfig Pusher config details
   * @param channels Channels to trigger the event on
   * @param eventName Name of the event
   * @param data Data to send
   * @param socketId Socked ID to exclude
   * @return PusherResponse
   */
  def trigger(pusherConfig: PusherConfig,
              channels: List[String],
              eventName: String,
              data: String,
              socketId: Option[String] = None): PusherResponse = {
    val triggerData: TriggerData = TriggerData(channels, eventName, data, socketId)
    val requestParams: RequestParams =
      RequestParams(pusherConfig, "POST", "/events", None, Some(encodeTriggerData(triggerData)))

    val validators = List(
      StringValidator(validateEventName, eventName),
      StringValidator(validateDataLength, data),
      ListValidator(validateChannelCount, channels)
    )

    Request.validateAndMakeRequest(requestParams, validators)
  }

  /**
   * Get information for multiple channels
   * @param pusherConfig Pusher config details
   * @param prefixFilterOpt Prefix to filter channels with
   * @param attributesOpt Attributes to be returned for each channel
   * @return PusherResponse
   */
  def channelsInfo(pusherConfig: PusherConfig,
                   prefixFilterOpt: Option[String],
                   attributesOpt: Option[List[String]]): PusherResponse = {

    val attributeParams: Map[String, String] = attributesOpt.map(
      attributes => Map("info" -> attributes.mkString(","))
    ).getOrElse(Map.empty[String, String])

    val prefixParams: Map[String, String] = prefixFilterOpt.map(
      prefixFilter => Map("filter_by_prefix" -> prefixFilter)
    ).getOrElse(Map.empty[String, String])

    Request.makeRequest(
      RequestParams(
        pusherConfig,
        "GET",
        "/channels",
        Some(attributeParams ++ prefixParams),
        None
      )
    )
  }

  /**
   * Get info for one channel
   * @param pusherConfig Pusher config details
   * @param channel Name of channel
   * @param attributes Attributes requested
   * @return PusherResponse
   */
  def channelInfo(pusherConfig: PusherConfig,
                  channel: String,
                  attributes: Option[List[String]]): PusherResponse = {
    val params: Map[String, String] =
      if (attributes.isDefined) {
        Map("info" -> attributes.get.mkString(","))
      } else Map()

    val requestParams: RequestParams =
      RequestParams(pusherConfig, "GET", s"/channels/$channel", Some(params), None)

    Request.makeRequest(requestParams)
  }

  /**
   * Fetch user id's subscribed to a channel
   * @param pusherConfig Pusher config details
   * @param channel Name of channel
   * @return PusherResponse
   */
  def usersInfo(pusherConfig: PusherConfig, channel: String): PusherResponse = {
    val requestParams: RequestParams =
      RequestParams(pusherConfig, "GET", s"/channels/$channel/users", None, None)
    val validators = List(StringValidator(validateChannel, channel))

    Request.validateAndMakeRequest(requestParams, validators)
  }

  /**
   * Generate a delegated client subscription token
   * @param pusherConfig Pusher config details
   * @param channel Channel to authenticate
   * @param socketId SocketId that required auth
   * @param customDataOpt Used on presence channels for info
   * @return String
   */
  def authenticate(pusherConfig: PusherConfig,
                   channel: String,
                   socketId: String,
                   customDataOpt: Option[Map[String, String]]): String = {

    val stringToSign: String = customDataOpt.map(
      customData => {
        s"$socketId:$channel:${encodeJson(customData)}"
      }
    ).getOrElse(s"$socketId:$channel")

    val signature: String = sign(pusherConfig.secret, stringToSign)
    val auth: String = s"${pusherConfig.key}:$signature"
    val result: Map[String, String] = Map(
      "auth" -> auth
    )

    encodeJson(
      result ++ customDataOpt.map(
        customData => Map("channel_data" -> encodeJson(customData))
      ).getOrElse(Map.empty[String, String])
    )
  }

  // TODO
  // Could probably be improved by returning an Either
  // with an error message explaining why this was failed
  /**
   * Validate webhook messages
   * @param pusherConfig Pusher config details
   * @param key Key used to sign the body
   * @param signature Signature given with the body
   * @param body Body that needs to be verified
   * @return Option
   */
  def validateWebhook(pusherConfig: PusherConfig,
                      key: String,
                      signature: String,
                      body: String): Option[Map[String, Any]] = {
    if (key != pusherConfig.key) return None

    if (!verify(pusherConfig.secret, body, signature)) return None

    val bodyData = decodeJson(body)
    val timeMs = bodyData.get("time_ms")

    if (timeMs.isEmpty) return None

    timeMs match {
      case Some(time: Int) =>
        if ((System.currentTimeMillis / 1000 - time) > 300000) return None
      case Some(time: Any) => return None
      case None => return None
    }

    Some(bodyData)
  }
}
