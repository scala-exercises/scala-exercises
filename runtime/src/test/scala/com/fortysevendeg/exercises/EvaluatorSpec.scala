/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.runtime

import scala.concurrent.duration._
import org.scalatest._

class EvaluatorSpec extends FunSpec with Matchers {

  describe("evaluation") {
    it("fails with a timeout when takes longer than the configured timeout") {
      val evaluator = new Evaluator(1 second)
      val result: EvalResult[Int] = evaluator("", "{ while(true) {}; 123 }")
      result should matchPattern {
        case t: EvalResult.Timeout[Int] ⇒
      }
    }
  }
}
