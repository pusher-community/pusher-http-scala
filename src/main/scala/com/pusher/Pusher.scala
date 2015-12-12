package com.pusher

import com.pusher.RequestValidator.{
  validateChannel,
  validateEventName,
  validateChannelCount,
  validateDataLength
}

import com.pusher.Util.{encodeTriggerData, encodePresenceUser, encodeJson}

import com.pusher.Types.PusherResponse
import com.pusher.Signature.{sign, verify}

/**
 * Pusher object
 */
class Pusher(private val appId: String,
             private val key: String,
             private val secret: String,
             private val ssl: Boolean = true,
             private val cluster: Option[String] = None,
             private val port: Option[Int] = None,
             private val host: Option[String] = None) {

  private val pusherConfig: PusherConfig = PusherConfig(appId, key, secret, ssl, cluster, port, host)

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
              socketId: Option[String] = None): PusherResponse[TriggerResponse] = {
    val triggerData: TriggerData = TriggerData(channels, eventName, data, socketId)

    val validationResults = List(
      validateEventName(eventName),
      validateDataLength(data),
      validateChannelCount(channels),
      channels.map(c => validateChannel(c)).head
    ).flatMap(x => x)

    if (validationResults.nonEmpty) return Left(validationResults.head)

    val requestParams = RequestParams(
      pusherConfig,
      "POST",
      "/events",
      None,
      Some(encodeTriggerData(triggerData))
    )

    Request.buildPusherResponse[TriggerResponse](
      Right(requestObject(requestParams).rawResponse())
    )
  }

  /**
   * Get information for multiple channels
   * @param prefixFilterOpt Prefix to filter channels with
   * @param attributesOpt Attributes to be returned for each channel
   * @return PusherResponse
   */
  def channelsInfo(prefixFilterOpt: Option[String],
                   attributesOpt: Option[List[String]]): PusherResponse[ChannelsInfoResponse] = {
    val attributeParams: Map[String, String] = attributesOpt.map(
      attributes => Map("info" -> attributes.mkString(","))
    ).getOrElse(Map.empty[String, String])

    val prefixParams: Map[String, String] = prefixFilterOpt.map(
      prefixFilter => Map("filter_by_prefix" -> prefixFilter)
    ).getOrElse(Map.empty[String, String])

    val requestParams = RequestParams(
      pusherConfig,
      "GET",
      "/channels",
      Some(attributeParams ++ prefixParams),
      None
    )

    Request.buildPusherResponse[ChannelsInfoResponse](
      Right(requestObject(requestParams).rawResponse())
    )
  }

  /**
   * Get info for one channel
   * @param channel Name of channel
   * @param attributes Attributes requested
   * @return PusherResponse
   */
  def channelInfo(channel: String,
                  attributes: Option[List[String]]): PusherResponse[ChannelInfoResponse] = {
    val params: Map[String, String] =
      if (attributes.isDefined) {
        Map("info" -> attributes.get.mkString(","))
      } else Map.empty[String, String]

    val requestParams = RequestParams(
      pusherConfig,
      "GET",
      s"/channels/$channel",
      Some(params),
      None
    )

    Request.buildPusherResponse[ChannelInfoResponse](
      Right(requestObject(requestParams).rawResponse())
    )
  }

  /**
   * Fetch user id's subscribed to a channel
   * @param channel Name of channel
   * @return PusherResponse
   */
  def usersInfo(channel: String): PusherResponse[UsersInfoResponse] = {
    val validationResult = validateChannel(channel)
    if (validationResult.nonEmpty) return Left(validationResult.get)

    val requestParams = RequestParams(
      pusherConfig,
      "GET",
      s"/channels/$channel/users",
      None,
      None
    )

    Request.buildPusherResponse[UsersInfoResponse](
      Right(requestObject(requestParams).rawResponse())
    )
  }

  /**
   * Generate a delegated client subscription token
   * @param channel Channel to authenticate
   * @param socketId SocketId that required auth
   * @param customDataOpt Used on presence channels for info
   * @return String
   */
  def authenticate(channel: String,
                   socketId: String,
                   customDataOpt: Option[PresenceUser]): String = {
    val stringToSign: String = customDataOpt.map(
      customData => s"$socketId:$channel:${encodePresenceUser(customData)}"
    ).getOrElse(s"$socketId:$channel")

    val signature: String = sign(pusherConfig.secret, stringToSign)
    val auth: String = s"${pusherConfig.key}:$signature"
    val result: Map[String, String] = Map("auth" -> auth)

    encodeJson(
      result ++ customDataOpt.map(
        customData => Map("channel_data" -> encodePresenceUser(customData))
      ).getOrElse(Map.empty[String, String])
    )
  }

  /**
   * Validate webhook messages
   * @param key Key used to sign the body
   * @param signature Signature given with the body
   * @param body Body that needs to be verified
   * @return PusherResponse
   */
  def validateWebhook(key: String,
                      signature: String,
                      body: String): PusherResponse[WebhookResponse] = {
    if (key != pusherConfig.key) return Left(WebhookError("Key's did not match when verifying webhook"))

    if (!verify(pusherConfig.secret, body, signature)) {
      return Left(WebhookError("Received webhook with invalid signature"))
    }

    val bodyData = Request.buildPusherResponse[WebhookResponse](Left(body))
    bodyData match {
      case Right(data) =>
        if ((System.currentTimeMillis / 1000 - data.time_ms) > 300000) {
          return Left(WebhookError("Webhook time not within 300 seconds"))
        }

        Right(data)
      case Left(error) => Left(error)
    }
  }

  /**
   * Get a new Request object
   * @param requestParams RequestParams
   * @return Request
   */
  def requestObject(requestParams: RequestParams) = Request(requestParams)
}
