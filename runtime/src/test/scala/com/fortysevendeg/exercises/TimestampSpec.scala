/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.runtime

import org.scalatest._

class TimestampSpec extends FunSpec with Matchers {
  describe("Timestamp") {
    it("works in both directions") {
      val date = new java.util.Date()

      val timestamp         = Timestamp.fromDate(date)
      val dateFromTimestamp = Timestamp.toDate(timestamp)

      Timestamp.fromDate(date) shouldEqual Timestamp.fromDate(dateFromTimestamp)
    }
  }
}
