package com.example

import akka.actor.{Actor, Props, _}
import spray.can.Http
import spray.http.HttpMethods._
import spray.http._
import spray.routing.HttpService

import scala.util.Try

class HttpPusher extends Actor with HttpService with ActorLogging {

  def receive = {
    case _: Http.Connected => sender ! Http.Register(self)

    case r@HttpRequest(GET, Uri.Path(path), _, _, _) =>

      val batchSize: Int = r.uri.query.get("batchSize") match {
        case Some(i) => Try(i.toInt).getOrElse(100)
        case None => 100
      }
      val startId: Int = r.uri.query.get("startId") match {
        case Some(i) => Try(i.toInt).getOrElse(1)
        case None => 1
      }
      log.info(s"serving request for ${path} with startId: ${startId}, batch size: ${batchSize}")

      val peer = sender // freeze the sender

      path match {
        case "/serial" => context actorOf Props(new FetcherSupervisor("sa", peer, startId = 1, batchSize))
        case "/parallel" => context actorOf Props(new FetcherSupervisor("oe", peer, startId = 1, batchSize))
        case "/super" => context actorOf Props(new FetcherSupervisor("ta", peer, startId = 1, batchSize))
        case "/fwd" => context actorOf Props(new FetcherSupervisor("vh", peer, startId = 1, batchSize))
        case _ => sender ! HttpResponse(entity = "WAT? this is not a valid endpoint")
      }
  }

  def calcIds(startId: Int, batchSize: Int): Set[Int] = {
    val finalId: Int = startId + batchSize - 1
    (startId to finalId).to[Set]
  }

  def calcParallelSliceSize(overallBatchSize: Int): Int = {
    Math.max(overallBatchSize / 20, 100)
  }

  override implicit def actorRefFactory: ActorRefFactory = context
}