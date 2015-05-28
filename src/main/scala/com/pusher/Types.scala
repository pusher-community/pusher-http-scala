package com.pusher

/**
 * Custom types
 */
object Types {

  type PusherResponse = Either[PusherException, Map[String, Any]]

}

/**
 * Custom type for Trigger data
 * @param channels List of channels
 * @param eventName Name of event
 * @param data Data to be send
 * @param socketId SocketId if any
 */
case class TriggerData(channels: List[String],
                       eventName: String,
                       data: String,
                       socketId: Option[String])