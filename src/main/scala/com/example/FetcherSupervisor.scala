package com.example

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import com.example.data.DataProvider
import spray.can.Http
import spray.http.{ChunkedMessageEnd, ChunkedResponseStart, HttpResponse, MessageChunk}

import scala.collection.immutable.IndexedSeq

case class Ok(remaining: Int)

object StartBatch

object BatchDone

case class ChunkReady(data: String)

class FetcherSupervisor(entity: String, client: ActorRef, startId: Int, batchSize: Int) extends Actor with ActorLogging {
  log.debug("Starting streaming supervisor ...")

  val finalId: Int = startId + batchSize - 1

  val maxSize = 10

  val idGroups: Seq[IndexedSeq[Int]] = (startId to finalId).grouped(maxSize).to[Seq]

  val chunkQueue: collection.mutable.Queue[String] = collection.mutable.Queue[String]()

  var sentChunks = 0

  var children: Seq[ActorRef] = idGroups map { group =>
    context actorOf Props(new ChunkyFetcherToSupervisor(new DataProvider, group.to[Set], self, client))
  }

  children foreach {_ ! StartBatch}

  client ! ChunkedResponseStart(HttpResponse(entity = "")).withAck(Ok)

  def receive = {

    case ChunkReady(data) => {
      log.info(s"forwarding chunk: $data")
      chunkQueue.enqueue(data)
      self ! Ok // TODO - can this lead to chunks 'racing' against each other?
    }
    case Ok => {
      sendChunk
    }

    case BatchDone => {
      log.info(s"received BatchDone from $sender")

      children = children.filter(_ != sender)

      if (children.isEmpty) {
        // TODO - use become to change to draining only
        log.info("all children are done, waiting for termination")
        log.info(s"sent $sentChunks chunks, expected to send $batchSize chunks. ${chunkQueue.size} chunks still queued")

        // TODO - sending last message chunks can happen after ChunkedMessageEnd is dispatched
        chunkQueue.foreach(_ => sendChunk)

        client ! ChunkedMessageEnd
      }
    }

    case x: Http.ConnectionClosed =>
      log.warning(s"Canceling response stream due to $x ...")
      context.stop(self)
  }

  private def sendChunk: Unit = {
    if(chunkQueue.isEmpty){
      log.warning("attempting to send a chunk, but the queue is empty!")
      return
    }
    sentChunks += 1
    val chunk: String = chunkQueue.dequeue()
    log.debug(s"Sending response chunk to client: $chunk")
    client ! MessageChunk(chunk).withAck(Ok)
  }
}
