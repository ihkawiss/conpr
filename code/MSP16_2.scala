package le

import akka.actor._
import scala.concurrent.Future
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import scala.concurrent.Await
import java.util.concurrent.TimeoutException

object MSP16_2 {

  val as = ActorSystem("as")

  def main(args: Array[String]) {
    val maxActor = as.actorOf(Props[FindMaxActor])
    val values = Array.range(1, 5000000)

    implicit val timeout = Timeout(20 seconds)
    val resultFuture: Future[Any] = (maxActor ? StartSearch(0, values.length, values))

    try {
      val result = Await.result(resultFuture, 1 seconds)
      println("Got final Result: " + result)
    } catch {
      case ex: TimeoutException => { println("Timeout") }
    }

  }

  case class StartSearch(start: Int, end: Int, values: Array[Int])
  case class DeliverResult(result: Int)

  class FindMaxActor extends Actor {
    var awaitResults: Int = 1
    var currentMax = 0

    var initialSender: ActorRef = null

    def receive: Actor.Receive = {
      // perform search for max value
      case StartSearch(start, end, values) if end - start < 32 /* 32 -> way to low, overhead */ => {
        if (initialSender == null) { initialSender = sender }

        var max = 0
        for (i <- start to end - 1) {
          if (max < values(i)) {
            max = values(i)
          }
        }

        sender ! DeliverResult(max)
      }

      // split task into two actors
      case StartSearch(start, end, values) => {
        if (initialSender == null) { initialSender = sender }

        val itemsPerActor = (end - start) / 2

        // create actors for work sharing
        val left = context.actorOf(Props[FindMaxActor], "left")
        val right = context.actorOf(Props[FindMaxActor], "right")

        awaitResults = 2

        left ! StartSearch(start, start + itemsPerActor, values)
        right ! StartSearch(start + itemsPerActor, end, values)
      }

      case DeliverResult(result) => {
        if (currentMax < result) {
          currentMax = result
        }

        awaitResults -= 1

        if (awaitResults == 0) {
          initialSender ! DeliverResult(currentMax)
        }
      }

      case _ => {
        println("ERROR: no handler for message found")
      }

    }
  }

}