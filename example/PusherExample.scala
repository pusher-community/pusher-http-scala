import com.pusher._

object PusherExample {
  def main (args: Array[String]) {
    val config: PusherConfig = PusherConfig("124615", "224dd379428b25b8a1e4", "76ab1c5e5b47b22ad0b2")

    val result = Pusher.trigger(config, List("test_channel"), "test_event", "test_event")
    result match {
      case Left(error) => println(error)
      case Right(res) => println(res)
    }
  }
}

