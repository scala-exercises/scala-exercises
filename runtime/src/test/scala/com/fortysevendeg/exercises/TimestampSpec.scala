package com.fortysevendeg.exercises

import org.scalatest._

class TimestampSpec extends FunSpec with Matchers {
  describe("Timestamp") {
    it("works in both directions") {
      val date = new java.util.Date()

      val timestamp = Timestamp.fromDate(date)
      val dateFromTimestamp = Timestamp.toDate(timestamp)

      Timestamp.fromDate(date) shouldEqual Timestamp.fromDate(dateFromTimestamp)
    }
  }
}

