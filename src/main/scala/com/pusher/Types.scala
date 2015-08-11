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
 * Channel details
 * @param user_count Number of users in the channel
 */
case class ChannelDetails(user_count: Option[Int])

/**
 * Contains information about channels
 * @param channels Channels mapped to ChannelDetails
 */
case class ChannelsInfoResponse(channels: Map[String, ChannelDetails]) extends PusherBaseResponse

/**
 * Information for one channel
 * @param occupied Indicates if the channel is occupied or not
 * @param user_count Number of users in the channel
 * @param subscription_count Number of subscriptions to the channel
 */
case class ChannelInfoResponse(occupied: Boolean,
                               user_count: Option[Int],
                               subscription_count: Option[Int]) extends PusherBaseResponse

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
class WebhookEvent

/**
 * Contains webhook body data
 * @param time_ms Webhook time
 * @param events List of events
 */
case class WebhookResponse(time_ms: Long, events: List[Map[String, String]]) extends PusherBaseResponse

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
