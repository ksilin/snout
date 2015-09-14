package com.example

import akka.actor.{Actor, ActorLogging, ActorRef}
import com.example.data.DataProvider
import spray.can.Http
import spray.http.{ChunkedMessageEnd, ChunkedResponseStart, HttpResponse, MessageChunk}

class ParallelStreamer(entity: String, client: ActorRef, ids: Set[Int], batchSize: Int = 10) extends Actor with ActorLogging {
  log.debug("Starting parallel response ...")

  export(ids.grouped(batchSize))

  def receive = {
    case x: Http.ConnectionClosed =>
      log.warning(s"Canceling response stream due to $x")
      context.stop(self)
  }

  def export(idGroups: Iterator[Set[Int]]): Unit = {
    client ! ChunkedResponseStart(HttpResponse(entity = ""))
    idGroups.to[Seq].par foreach { group =>
      sendChunks(group, new DataProvider)
    }
    client ! ChunkedMessageEnd
  }

  private def sendChunks(ids: Set[Int], dataProvider: DataProvider): Unit = {
    ids foreach { id =>
      log.debug(s"Sending response chunk with id $id")
      dataProvider.getStringForId(id) match {
        case None => log.info(s"no data found for id $id")
        case Some(data) => {
          val msg = data.toString() + "\n"
          client ! MessageChunk(msg)
        }
      }
    }
  }
}
