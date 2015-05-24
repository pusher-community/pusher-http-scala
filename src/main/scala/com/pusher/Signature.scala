package com.pusher

import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

object Signature {
  /**
   * Sign a string with a secret using HMAC SHA256
   * @param secret Secret to be used to sign
   * @param stringToSign String to sign
   * @return String
   */
  def sign(secret: String, stringToSign: String): String = {
    val SHA256 = "HmacSHA256"
    val mac = Mac.getInstance(SHA256)
    mac.init(new SecretKeySpec(secret.getBytes, SHA256))

    mac.doFinal(stringToSign.getBytes).toString
  }

  /**
   * Verify if the signature is correct
   * @param secret Secret to be used to sign
   * @param stringToSign String to sign
   * @param signature Computed signature
   * @return
   */
  def verify(secret: String, stringToSign: String, signature: String): Boolean = {
    if (sign(secret, stringToSign) == signature) {
      true
    } else {
      false
    }
  }
}
