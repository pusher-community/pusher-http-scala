package com.pusher

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

object Signature {

  val SHA256 = "HmacSHA256"

  /**
   * Sign a string with a secret using HMAC SHA256
   * @param secret Secret to be used to sign
   * @param stringToSign String to sign
   * @return String
   */
  def sign(secret: String, stringToSign: String): String = {
    val mac = Mac.getInstance(SHA256)
    mac.init(new SecretKeySpec(secret.getBytes, SHA256))

    mac.doFinal(stringToSign.getBytes).map("%02x".format(_)).mkString
  }

  /**
   * Verify if the signature is correct
   * @param secret Secret to be used to sign
   * @param stringToSign String to sign
   * @param signature Computed signature
   * @return Boolean
   */
  def verify(secret: String, stringToSign: String, signature: String): Boolean = {
    sign(secret, stringToSign) == signature
  }
}
