package com.example

import org.specs2.execute.{Success, AsResult}

trait SpecHelper {

  def timed[A](s: String)(x: ⇒ A): Double = {
    val start = System.nanoTime()
    var ms = 0.0
    try x finally {
      val end = System.nanoTime()
      ms = (end - start) / 1000000.0
      println(f"$s took [$ms%.3f] ms")
    }
    ms
  }

  implicit def anyToSuccess[T]: AsResult[T] = new AsResult[T] {
    def asResult(t: =>T) = {
      t
      Success()
    }
  }

}
