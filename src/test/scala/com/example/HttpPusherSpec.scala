package com.example

import java.net.InetSocketAddress

import akka.actor.{ActorRef, UnhandledMessage, ActorSystem, Props}
import akka.event.Logging.LogEvent
import akka.pattern.ask
import akka.testkit._
import akka.util.Timeout
import spray.can.Http
import scala.concurrent.duration._
import org.specs2.mutable.SpecificationLike
import spray.http.{Uri, HttpResponse, HttpMethods, HttpRequest}

import scala.concurrent.{Await, Future}

class HttpPusherSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions with SpecHelper{

  // without being sequential, tests fail randomly
  sequential

  val actor = TestActorRef[HttpPusher]

  implicit val timeout = Timeout(5.seconds)

  "HttpPusher" should {

    "respond with Http.Register to Http.Connected" in {
      val pusher = system.actorOf(Props[HttpPusher], "pusher")
      pusher ! new Http.Connected(new InetSocketAddress("localhost", 1234), new InetSocketAddress("localhost", 5678))
      expectMsg(Http.Register(pusher))
    }
  }

  "Actor" should {

    val expectedResponse = HttpResponse(entity = "WAT? this is not a valid endpoint")
    val req: HttpRequest = HttpRequest(HttpMethods.GET)

    "be testable synchronously" in {
      val future: Future[Any] = actor ? req
      val response: HttpResponse = Await.result(future, 5.seconds).asInstanceOf[HttpResponse]
      response shouldEqual expectedResponse
    }

    val expectedMsg: String = "serving request for sa with startId: 1, batch size: 100"
    val reqSa = HttpRequest(HttpMethods.GET, Uri("sa"))

    "be testable through the EventFilter and a custom interceptor" in {
      val interceptor: PartialFunction[LogEvent, Boolean] = {
        case x => {
          val message: String = x.message.asInstanceOf[String]
          message.contains(expectedMsg)
        }
      }
      EventFilter.custom(test = interceptor, occurrences = 1) intercept {actor ! reqSa}
    }

    "be testable through the EventFilter and a msg pattern" in {
      EventFilter.info(pattern = expectedMsg, occurrences = 1) intercept {actor ! reqSa}
    }

    "be testable through msg expectation" in {
      actor ! req
      expectMsg(expectedResponse)
    }
  }
}
