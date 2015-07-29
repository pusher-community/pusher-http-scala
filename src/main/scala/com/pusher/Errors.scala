package com.pusher

/**
 * Base error trait
 */
sealed trait PusherError {
  def message: String
}

/**
 * Error for a Pusher bad request
 * @param message Message for the error
 */
case class PusherBadRequestError(message: String) extends PusherError

/**
 * Error for a Pusher bad authorization
 * @param message Message for the error
 */
case class PusherBadAuthError(message: String) extends PusherError

/**
 * Error for a Pusher forbidden request
 * @param message Message for the error
 */
case class PusherForbiddenError(message: String) extends PusherError

/**
 * Error for a Pusher unexpected status
 * @param message Message for the error
 */
case class PusherBadStatusError(message: String) extends PusherError

/**
 * Error if an HTTP request to Pusher fails
 * @param message Message for the error
 */
case class PusherRequestFailedError(message: String) extends PusherError

/**
 * Validation error class
 * @param message Error message
 */
case class ValidationError(message: String) extends PusherError

/**
 * Error case class for Webhooks
 * @param message Error message
 */
case class WebhookError(message: String) extends PusherError

/**
 * Error for JSON parsing failures
 * @param message Error message
 */
case class JSONParsingError(message: String) extends PusherError

