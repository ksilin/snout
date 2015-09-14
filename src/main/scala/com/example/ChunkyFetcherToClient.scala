package com.example

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.example.data.DataProvider
import spray.http.MessageChunk

import scala.concurrent.duration._

class ChunkyFetcherToClient(dataProvider: DataProvider, ids: Set[Int], supervisor: ActorRef, client: ActorRef) extends Actor with ActorLogging {
  log.debug(s"Start fetching: $ids")

  def start(): Unit = {
    import context.dispatcher
    ids.foreach { id =>
      dataProvider.getStringForId(id) match {
        case None =>
        case Some(data) => {
          val msg = data + "\n"
          log.debug(s"Sending response chunk with id $id to client")
          client ! MessageChunk(msg)
        }
      }
    }
    log.debug(s"batch done: $ids")
    context.system.scheduler.scheduleOnce(100 millis, supervisor, BatchDone)
    context.stop(self)
  }

  def receive = {
    case StartBatch => start()
  }
}
