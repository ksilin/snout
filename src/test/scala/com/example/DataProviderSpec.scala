package com.example

import com.example.data.DataProvider
import org.specs2.mutable.Specification


class DataProviderSpec extends Specification with SpecHelper{

  "DataProvider" should {

    val testId = 10380369
    val getter: DataProvider = new DataProvider

    "return an oe for an existing id" in {
      val result: String = getter.getStringForId(testId).get
      result === "1780054039"
    }

    "should maintain the schedule" in {
      val reps: Int = 5
      val time: Double = timed(s"retrieving $reps chunks") {
        (0 until reps).foreach {
          getter.getStringForId
        }
      }
      time must beCloseTo(500.0 +/- 100.0)
    }

  }
}
