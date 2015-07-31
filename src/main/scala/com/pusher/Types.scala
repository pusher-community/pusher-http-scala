package com.pusher

/**
 * Custom types
 */
object Types {
  type PusherResponse[T] = Either[PusherError, T]

  type ValidationResponse = Option[ValidationError]
}

/**
 * The base class all Response classes inherit from
 */
class PusherBaseResponse

/**
 * Case class for deserializing calls to `trigger`
 */
case class TriggerResponse() extends PusherBaseResponse

/**
 * User count for channels
 * @param count Number of users in the channel
 */
case class UserCount(count: Int)

/**
 * ChannelDetails for use in `ChannelsInfoResponse`
 * @param name Name of the channel
 */
case class ChannelDetails(name: Option[UserCount])

/**
 * Contains information about channels
 * @param channels Channels mapped to ChannelDetails
 */
case class ChannelsInfoResponse(channels: ChannelDetails) extends PusherBaseResponse

/**
 * Information for one channel
 * @param occupied Indicates if the channel is occupied or not
 * @param userCount Number of users in the channel
 * @param subscriptionCount Number of subscriptions to the channel
 */
case class ChannelInfoResponse(occupied: Boolean,
                               userCount: Option[Int],
                               subscriptionCount: Option[Int]) extends PusherBaseResponse

/**
 * User details for use in `UsersInfoResponse`
 * @param id Id of the user
 */
case class UserDetails(id: String)

/**
 * Users in a channel
 * @param users List of `UserDetails`
 */
case class UsersInfoResponse(users: List[UserDetails]) extends PusherBaseResponse

/**
 * Class from which all webhook events inherit
 */
class WebhookEvents

/**
 * Contains webhook body data
 * @param timeMs Webhook time
 * @param events List of events
 */
case class WebhookResponse(timeMs: Long, events: List[WebhookEvents]) extends PusherBaseResponse

/**
 * Webhook channel existence data
 * @param name Name of event
 * @param channel Name of channel
 */
case class WebhookChannelExistenceEvent(name: String, channel: String) extends WebhookEvents

/**
 * Webhook presence data
 * @param name Name of event
 * @param channel Name of channel
 * @param userId User ID of the subscribed user
 */
case class WebhookPresenceEvent(name: String, channel: String, userId: String) extends WebhookEvents

/**
 * Client event data
 * @param name Name of event
 * @param channel Name of channel
 * @param event Name of event
 * @param data Data sent with the client event
 * @param socketId Socket ID of the sending socket
 * @param userId Optional user id for presence channels
 */
case class WebhookClientEvent(name: String,
                              channel: String,
                              event: String,
                              data: String,
                              socketId: String,
                              userId: Option[String])

/**
 * Custom type for Trigger data
 * @param channels List of channels
 * @param eventName Name of event
 * @param data Data to be sent
 * @param socketId SocketId if any
 */
case class TriggerData(channels: List[String],
                       eventName: String,
                       data: String,
                       socketId: Option[String])


/**
 * Config for Pusher
 * @param appId Pusher application id
 * @param key Pusher application key
 * @param secret Pusher application secret
 * @param ssl If SSL should be used or not
 * @param cluster The cluster to connect to
 * @param port The port to use when connecting
 * @param host Host to connect to
 */
case class PusherConfig(appId: String,
                        key: String,
                        secret: String,
                        ssl: Boolean = true,
                        cluster: Option[String] = None,
                        port: Option[Int] = None,
                        host: Option[String] = None) {
  /**
   * Get scheme based on ssl
   * @return String
   */
  def scheme: String = {
     ssl match {
      case true => "https"
      case false => "http"
    }
  }

  /**
   * Return the port based on config
   * @return Int
   */
  def getPort: Int = port.getOrElse(if (ssl) 443 else 80)

  /**
   * Returns the host config
   * @return String
   */
  def getHost: String = {
    host match {
      case Some(h) => h
      case None =>
        cluster match {
          case Some(c) => s"api-$c.pusher.com"
          case None => "api.pusherapp.com"
        }
    }
  }
}

/**
 * Request options
 * @param config PusherConfig object
 * @param verb Verb for the request
 * @param path Path to make the request to
 * @param params Optional parameters to be added to the request
 * @param body Optional body for the request
 */
case class RequestParams(config: PusherConfig,
                         verb: String,
                         path: String,
                         params: Option[Map[String, String]],
                         body: Option[String])
