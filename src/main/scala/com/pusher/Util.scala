package com.pusher

object Util {
  /**
   * Validate required params for Pusher
   * @param credentialMap Map containing pusher credentials
   */
  def checkEmptyCredentials(credentialMap: Map[String, String]): Unit = {
    credentialMap.foreach {
      case(k, v) =>
        if (v.isEmpty) throw new Exception("Empty credential " + k)
    }
  }
}
