import com.pusher._

object PusherExample {
  def main (args: Array[String]) {
    val pusher = new Pusher("YOUR_APP_ID", "YOUR_KEY", "YOUR_SECRET")
    val result = pusher.trigger(List("test_channel"), "test_event", "test_event")
    result match {
      case Left(error) => println(error)
      case Right(res) => println(res)
    }
  }
}
