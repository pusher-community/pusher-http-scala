package com.pusher

/**
 * Base class that inherits from Exception
 * @param message Exception message
 */
abstract class PusherException(message: String) extends Exception

/**
 * Exception for a Pusher bad request
 * @param message Message for the exception
 */
class PusherBadRequestException(message: String) extends PusherException(message)

/**
 * Exception for a Pusher bad authorization
 * @param message Message for the exception
 */
class PusherBadAuthException(message: String) extends PusherException(message)

/**
 * Exception for a Pusher forbidden request
 * @param message Message for the exception
 */
class PusherForbiddenException(message: String) extends PusherException(message)

/**
 * Exception for a Pusher unexpected status
 * @param message Message for the exception
 */
class PusherBadStatusException(message: String) extends PusherException(message)
