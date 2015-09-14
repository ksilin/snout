package com.example

import akka.actor._
import akka.event.Logging.LogEvent
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import com.example.data.DataProvider
import org.specs2.mutable.SpecificationLike
import spray.http._

import scala.concurrent.duration._

class ChunkyFetcherSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions {

  implicit val timeout = Timeout(5.seconds)

  class CatActor extends Actor with ActorLogging {
    def receive = {
      case x@MessageChunk(data, ext) => //println(s"received ${new String(x.data.toByteArray)}")
    }
  }

  "ChunkyFetcher" should {

    "async entity" in {
      val superProps = Props(new CatActor())
      val supervisor = system.actorOf(superProps, "supercat")
      val actorProps = Props(new ChunkyFetcherToSupervisor(dataProvider = new DataProvider, (1 to 10).to[Set], supervisor, supervisor))
      val actor = system.actorOf(actorProps, "fetcher_async")

      val interceptor: PartialFunction[LogEvent, Boolean] = {
        case x => {
          println(s"received LogEvent: $x")
          val message: String = x.message.asInstanceOf[String]
          message.contains("Sending response chunk with id")
        }
      }
      EventFilter.custom(test = interceptor, occurrences = 10) intercept {actor ! StartBatch}

      //      val supah = Await.result(system.actorSelection("/user").resolveOne(), 5 seconds)
      //      expectMsg(9 second, UnhandledMessage(BatchDone, supah, actor))
      //      expectMsg(9 second, UnhandledMessage(BatchDone, system.deadLetter, actor))
      //      expectMsg(BatchDone)
      //      EventFilter.info(pattern = "Sending response chunk with id*", occurrences = 1) intercept {actor ! Ok(1)}
      true
    }

  }
}
