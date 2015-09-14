package com.example

import akka.actor._
import akka.event.Logging.LogEvent
import akka.testkit._
import akka.util.Timeout
import org.specs2.matcher.Matchers
import org.specs2.mutable.SpecificationLike
import spray.http._

import scala.concurrent.duration._

class StreamerSupervisorSpec extends TestKit(ActorSystem()) with ImplicitSender
with SpecificationLike with DeactivatedTimeConversions with Matchers {

  //  isolated
  sequential

  implicit val timeout = Timeout(5.seconds)

  class CatActor extends Actor with ActorLogging {
    def receive = {
      case x@MessageChunk(data, ext) => log.info(s"cat actor received ${new String(x.data.toByteArray)}")
    }
  }

  class ClientActor extends Actor with ActorLogging {

    var done: Boolean = false
    var received: Int = 0

    def receive = {
      case x@ChunkedMessageEnd => {
        log.info(s"cat actor received ChunkedMessageEnd")
        done = true
        received += 1
      }
    }
  }

  val batchSize: Int = 3
  val clientProps = Props(new CatActor())

  "StreamerSupervisor" should {

    "receive all chunks prduced by children" in {
      val clientDouble = system.actorOf(clientProps, "cat1")

      val actorProps = Props(new FetcherSupervisor("sa", clientDouble, startId = 1, batchSize))

      val clientReceivedChunk: PartialFunction[LogEvent, Boolean] = {
        case x => {
          println(s"received LogEvent: $x")
          val message: String = x.message.asInstanceOf[String]
          message.contains("cat actor received")
        }
      }
      EventFilter.custom(test = clientReceivedChunk, occurrences = batchSize) intercept {
        system.actorOf(actorProps, "supervisor_msg_rcv")
      }
      true
    }

    "shut down after children are all done" in {
      val probe = TestProbe()

      val actorProps = Props(new FetcherSupervisor("sa", probe.ref, startId = 1, 1))

      system.actorOf(actorProps, "supervisor")

      import scala.reflect._
      within(10 seconds) {
        probe.expectMsgType(classTag[spray.http.Confirmed])
        probe.expectMsgType(classTag[MessageChunk])
        probe.expectMsg(ChunkedMessageEnd)
      }
      true
    }

    "shut down after children are all done 2" in {
      val clientDouble = TestActorRef(new ClientActor())

      val actorProps = Props(new FetcherSupervisor("sa", clientDouble, startId = 1, 1))

      system.actorOf(actorProps, "super2")

      val clientActor = clientDouble.underlyingActor

      awaitCond(clientActor.done, 10 seconds)

      // within does not work here for some reason, prehaps it does not check repeatedly
      // within(10 seconds) { clientActor.done should beTrue }
      true
    }

    "shut down after children are all done 3" in {
      val clientProps = Props(new ClientActor())
      val clientDouble = system.actorOf(clientProps, "cat2")

      val actorProps = Props(new FetcherSupervisor("sa", clientDouble, startId = 1, batchSize))

      val childrenDone: PartialFunction[LogEvent, Boolean] = {
        case x => {
          val message: String = x.message.asInstanceOf[String]
          message.contains("all children are done, waiting for termination")
        }
      }
      EventFilter.custom(test = childrenDone, occurrences = 1) intercept {
        system.actorOf(actorProps, "super3")
      }
      true
    }
  }
}
