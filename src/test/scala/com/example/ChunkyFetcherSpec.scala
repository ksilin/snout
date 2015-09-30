package com.example

import akka.actor._
import akka.event.Logging.LogEvent
import akka.testkit.{TestActorRef, EventFilter, ImplicitSender, TestKit}
import akka.util.Timeout
import com.example.data.DataProvider
import org.specs2.mutable.SpecificationLike
import spray.http._

import scala.concurrent.Await
import scala.concurrent.duration._

class ChunkyFetcherSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions with SpecHelper{

  implicit val timeout = Timeout(5.seconds)

  class CatActor extends Actor with ActorLogging {
    def receive = {
      case x@MessageChunk(data, ext) => //println(s"received ${new String(x.data.toByteArray)}")
    }
  }

//  val fake = TestActorRef[CatActor]
  val fake = system.actorOf(Props(new CatActor))

  "ChunkyFetcher" should {

    "async entity" in {
      val reps: Int = 5
      val actorProps = Props(new ChunkyFetcherToSupervisor(dataProvider = new DataProvider, (1 to reps).to[Set], fake, fake))
      val actor = system.actorOf(actorProps, "fetcher_async")

      val interceptor: PartialFunction[LogEvent, Boolean] = {
        case x => {
          println(s"event filter received LogEvent: $x")
          val message: String = x.message.asInstanceOf[String]
          message.contains("Sending response chunk with id")
        }
      }
      EventFilter.custom(test = interceptor, occurrences = reps) intercept {actor ! StartBatch}
      true
    }

    "be testable through expectating no answer" in {
      // TODO - I would have thought, the message will remain unhandled
      system.eventStream.subscribe(fake, classOf[DeadLetter])
      fake ! "hi"
      println("dead letters" + system.deadLetters.toString())
      expectNoMsg()
    }
  }
}
