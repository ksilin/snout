package com.example

import java.net.InetSocketAddress

import akka.actor.{ActorSystem, Props}
import akka.event.Logging.LogEvent
import akka.pattern.ask
import akka.testkit.{EventFilter, ImplicitSender, TestActorRef, TestKit}
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._
import org.specs2.mutable.SpecificationLike
import spray.http.{Uri, HttpResponse, HttpMethods, HttpRequest}

import scala.concurrent.{Await, Future}

class HttpPusherSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions {

  sequential

  val actor = TestActorRef[HttpPusher]

  implicit val timeout = Timeout(5.seconds)

  "HttpPusher" should {

    val req = HttpRequest(HttpMethods.GET)
    val reqSa = HttpRequest(HttpMethods.GET, Uri("sa"))

    val resp = HttpResponse(entity = "WAT? this is not a valid endpoint")

    "respond with Http.Register to Http.Connected" in {
      val pusher = system.actorOf(Props[HttpPusher], "pusher")
      pusher ! new Http.Connected(new InetSocketAddress("localhost", 1234), new InetSocketAddress("localhost", 5678))
      expectMsg(Http.Register(pusher))
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
