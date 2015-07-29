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
              socketId: Option[String] = None): PusherResponse[TriggerResponse] = {
    val triggerData: TriggerData = TriggerData(channels, eventName, data, socketId)

    val validationResults = List(
      validateEventName(eventName),
      validateDataLength(data),
      validateChannelCount(channels)
    )

    Request.validateAndMakeRequest(
      RequestParams(
        pusherConfig,
        "POST",
        "/events",
        None,
        Some(encodeTriggerData(triggerData))
      ),
      validationResults
    )
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
                   attributesOpt: Option[List[String]]): PusherResponse[ChannelsInfoResponse] = {
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
                  attributes: Option[List[String]]): PusherResponse[ChannelInfoResponse] = {
    val params: Map[String, String] =
      if (attributes.isDefined) {
        Map("info" -> attributes.get.mkString(","))
      } else Map.empty[String, String]

    Request.makeRequest(
      RequestParams(
        pusherConfig,
        "GET",
        s"/channels/$channel",
        Some(params),
        None
      )
    )
  }

  /**
   * Fetch user id's subscribed to a channel
   * @param pusherConfig Pusher config details
   * @param channel Name of channel
   * @return PusherResponse
   */
  def usersInfo(pusherConfig: PusherConfig, channel: String): PusherResponse[UsersInfoResponse] = {
    Request.validateAndMakeRequest(
      RequestParams(
        pusherConfig,
        "GET",
        s"/channels/$channel/users",
        None,
        None
      ),
      List(validateChannel(channel))
    )
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
    val result: Map[String, String] = Map("auth" -> auth)

    encodeJson(
      result ++ customDataOpt.map(
        customData => Map("channel_data" -> encodeJson(customData))
      ).getOrElse(Map.empty[String, String])
    )
  }

  /**
   * Validate webhook messages
   * @param pusherConfig Pusher config details
   * @param key Key used to sign the body
   * @param signature Signature given with the body
   * @param body Body that needs to be verified
   * @return PusherResponse
   */
  def validateWebhook(pusherConfig: PusherConfig,
                      key: String,
                      signature: String,
                      body: String): PusherResponse[WebhookResponse] = {
    if (key != pusherConfig.key) return Left(WebhookError("Key's did not match when verifying webhook"))

    if (!verify(pusherConfig.secret, body, signature)) return Left(WebhookError("Signatures do not match"))

    val bodyData = decodeJson(body)
    val timeMs = bodyData.get("time_ms")

    if (timeMs.isEmpty) return Left(WebhookError("No timestamp supplied with Webhook"))

    timeMs match {
      case Some(time: Int) =>
        if ((System.currentTimeMillis / 1000 - time) > 300000) {
          return Left(WebhookError("Webhook time not within 300 seconds"))
        }
      case Some(time: Any) => return Left(WebhookError("Invalid time format"))
      case None => return Left(WebhookError("No timestamp supplied with Webhook"))
    }

    Right(WebhookResponse(bodyData))
  }
}
