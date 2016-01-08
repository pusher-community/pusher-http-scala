import com.pusher._

import scala.concurrent.Await
import scala.util.{Failure, Success}
import scala.concurrent.ExecutionContext.Implicits.global

object PusherExample {
  def main (args: Array[String]) {
    val pusher = new Pusher("YOUR_APP_ID", "YOUR_KEY", "YOUR_SECRET")
    val result = pusher.trigger(List("test_channel"), "test_event", "test_event")
    result match {
      case Left(error) => println(error)
      case Right(res) => println(res)
    }

    val res = pusher.triggerAsync(List("test_channel"), "test_event", "test_event")
    res.onComplete {
      case Success(r) =>
        r match {
          case Left(err) => println(err)
          case Right(ans) => println(ans)
        }
      case Failure(ex) => println(s"Exception: $ex")
    }

    // Sleep so we can prolong the thread execution
    Thread.sleep(2000)
  }
}
