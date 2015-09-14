package com.example

import com.example.data.DataProvider
import org.specs2.mutable.Specification

class DataProviderSpec extends Specification {


  "DataProvider" should {

    val testOeId = 10380369

    val getter: DataProvider = new DataProvider

    "return an oe for an existing id" in {
      val result: String = getter.getStringForId(testOeId).get
      println(result)
      true
    }

//    "return correct articles" in {
//      val articles: List[String] = getter.findArtikelId(testOeId)
//      println("articles: " + articles)
//      articles must have size (1)
//      articles must contain(exactly("184"))
//    }

    // 6755 6045 4854 4994 6123 4786
    // 2988 2112 2330 2307 2049 2052 - after opt

    "should perform decently for a 100 items" in {

      (0 to 5).foreach { _ =>
        val start = System.currentTimeMillis()
        var found = 0
        (1 to 100) foreach { id =>
          getter.getStringForId(id).foreach({ _ => found += 1 })
        }
        val end = System.currentTimeMillis()
        val time = end - start
        println(s"exported $found oes in $time ms")
        //      time must be <(1000L)
      }
      true
    }

  }
}
