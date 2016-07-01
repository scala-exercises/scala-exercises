/*
 * scala-exercises-exercise-compiler
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises

import java.text.SimpleDateFormat
import java.util.{ Date, TimeZone }

object Timestamp {
  val ISO8601 = "yyyy-MM-dd'T'HH:mm:ssz"

  val UTC = TimeZone.getTimeZone("UTC")

  val FORMAT = new SimpleDateFormat(ISO8601) {
    setTimeZone(UTC)
  }

  def fromDate(d: Date): String = {
    FORMAT.format(d)
  }

  def toDate(timestamp: String): Date = FORMAT.parse(timestamp)
}
