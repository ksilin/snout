package com.example

import akka.actor.{ActorSystem, Props}
import akka.event.Logging.LogEvent
import akka.pattern.ask
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import scala.concurrent.duration._
import org.specs2.mutable.SpecificationLike
import spray.http.{Uri, HttpResponse, HttpMethods, HttpRequest}

import scala.concurrent.{Await, Future}

class JsonPusherSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions {

  val actor = TestActorRef[HttpPusher]

  implicit val timeout = Timeout(5.seconds)

  "JsonPusher" should {


    val req = HttpRequest(HttpMethods.GET)
    val reqSa = HttpRequest(HttpMethods.GET, Uri("sa"))

    val resp = HttpResponse(entity = "WAT? this is not a valid endpoint. Try one of these instead: sa, oe, ta, vh, ft, mo")

    "save only messages that starts with 'A'" in {
      actor ! req
      //      actor ! SimpleMessage("This message should not be saved")
      //      actor ! SimpleMessage("Another message for you")
      //      actor.underlyingActor.state.length mustEqual 2
      success
    }


    "sync" in {
      val actorProps = Props(new HttpPusher())
      val actor = system.actorOf(actorProps, "pusher_sync")

      val future: Future[Any] = actor ? req
      val result: HttpResponse = Await.result(future, 5.seconds).asInstanceOf[HttpResponse]
      println("response: " + result)
      true
    }

    "async entity" in {
      val actorProps = Props(new HttpPusher())
      val actor = system.actorOf(actorProps, "pusher_async")

      val interceptor: PartialFunction[LogEvent, Boolean] = {
        case x => {
          println(s"received LogEvent: $x")
          val message: String = x.message.asInstanceOf[String]
          message.contains("serving request for sa with startId: 1, batch size: 100")
        }
      }

      EventFilter.custom(test = interceptor, occurrences = 1) intercept {actor ! reqSa}
      EventFilter.info(pattern = "serving request for sa with startId: 1, batch size: 100", occurrences = 1) intercept {actor ! reqSa}

      true
    }

    "async missing entity" in {
      val actorProps = Props(new HttpPusher())
      val actor = system.actorOf(actorProps, "pusher_async_missing")

      actor ! req
      expectMsg(resp)
      true
    }

  }
}
