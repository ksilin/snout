package com.example.data

import scala.concurrent.{Await, Future, blocking}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

class DataProvider {
  def getStringForId(id: Int): Option[String] = {
    val eventualString: Future[String] = Future {
      blocking(Thread.sleep(100L));
      "done"
    }
    Await.result(eventualString, 1 second)
    Some((id * 999).toString)
  }
}
