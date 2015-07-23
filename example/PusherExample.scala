import com.pusher._

object PusherExample {
  def main (args: Array[String]) {
    val config: PusherConfig = PusherConfig("YOUR_APP_ID", "YOUR_KEY", "YOUR_SECRET")

    val result = Pusher.trigger(config, List("test_channel"), "test_event", "test_event")
    result match {
      case Left(error) => println(error)
      case Right(res) => println(res)
    }
  }
}
