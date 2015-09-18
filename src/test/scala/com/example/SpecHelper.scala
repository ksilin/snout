package com.example

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
}
