/*
 * Copyright 2014-2020 47 Degrees <https://47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.runtime

import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

object Timestamp {
  val ISO8601 = "yyyy-MM-dd'T'HH:mm:ssz"

  val UTC = TimeZone.getTimeZone("UTC")

  val FORMAT = new SimpleDateFormat(ISO8601) {
    setTimeZone(UTC)
  }

  def fromDate(d: Date): String =
    FORMAT.format(d)

  def toDate(timestamp: String): Date = FORMAT.parse(timestamp)
}
