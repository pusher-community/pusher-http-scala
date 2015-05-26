package com.pusher

/**
 * Custom types
 */
object Types {

  type PusherResponse = Either[PusherException, Map[String, Any]]

}
