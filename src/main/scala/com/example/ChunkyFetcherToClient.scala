package com.example

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.example.data.DataProvider
import spray.http.MessageChunk

class ChunkyFetcherToClient(dataProvider: DataProvider, ids: Set[Int], supervisor: ActorRef, client: ActorRef) extends Actor with ActorLogging {
  log.debug(s"Start fetching: $ids")

  val sentIds = collection.mutable.Buffer.empty[Int]
  val missingIds = collection.mutable.Buffer.empty[Int]

  def start(): Unit = {
    ids.foreach { id =>
      dataProvider.getStringForId(id) match {
        case None => missingIds += id
        case Some(data) => {
          val msg = data + "\n"
          log.debug(s"Sending response chunk for id $id to client")
          client ! MessageChunk(msg) // no ack
          sentIds += id
        }
      }
    }
    log.debug(s"sent data for ${sentIds.size} ids to client. No data found for ${missingIds.size} ids")
    supervisor ! BatchDone
    context.stop(self) // does the actor have to be stopped?
  }

  def receive = {
    case StartBatch => start()
  }
}
