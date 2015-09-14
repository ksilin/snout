package com.example

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import spray.can.Http

import scala.concurrent.duration._

object Boot extends App {

  implicit val system = ActorSystem("datapusher")

  val dbStreamPusher = system.actorOf(Props[HttpPusher], "http_pusher")

  implicit val timeout = Timeout(5.seconds)

  val config = ConfigFactory.load()
  val host = config.getString("http.host")
  val prt = config.getInt("http.port")
  IO(Http) ? Http.Bind(dbStreamPusher, interface = host, port = prt)
}
