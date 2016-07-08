/*
 * scala-exercises-runtime
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.runtime

import org.scalatest._

import MethodEval._

class MethodEvalSpec extends FunSpec with Matchers {

  describe("runtime evaluation") {

    val methodEval = new MethodEval()

    it("fails when incorrect parameter types are provided") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.intStringMethod",
        "\"hello\"" :: "\"world\"" :: Nil
      )
      assert(!res.didRun)
      assert(res.toSuccessXor.isLeft)
      assert(res.toExecutionXor.isLeft)
    }

    it("fails when too many parameters are provided") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.intStringMethod",
        "\"hello\"" :: "\"world\"" :: "1" :: Nil
      )
      assert(!res.didRun)
      assert(res.toSuccessXor.isLeft)
      assert(res.toExecutionXor.isLeft)
    }

    it("works when the parameters are appropriate") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.intStringMethod",
        "1 + 2" :: "\"world\"" :: Nil
      )

      res should equal(EvaluationSuccess[String]("3world"))
      assert(res.toSuccessXor.isRight)
      assert(res.toExecutionXor.isRight)
    }

    it("fails with assertion error when the parameters are incorrect") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.isOne",
        "2" :: Nil
      )

      res should matchPattern {
        case EvaluationException(_: TestFailedException) ⇒
      }
    }

    it("captures exceptions thrown by the called method") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.throwsExceptionMethod",
        Nil
      )

      res should matchPattern {
        case EvaluationException(_: ExampleTarget.ExampleException) ⇒
      }
      assert(res.toSuccessXor.isLeft)
      assert(res.toExecutionXor.isRight)
    }

    it("interprets imports properly") {
      val res = methodEval.eval(
        "org.scalaexercises.runtime",
        "org.scalaexercises.runtime.ExampleTarget.takesXorMethod",
        "Xor.right(1)" :: Nil,
        "import cats.data.Xor" :: Nil
      )

      res should equal(EvaluationSuccess[Boolean](true))
      assert(res.toSuccessXor.isRight)
      assert(res.toExecutionXor.isRight)
    }

    it("works when there are several concurrent evaluations") {
      def evalCalls() = {
        methodEval.eval(
          "org.scalaexercises.runtime",
          "org.scalaexercises.runtime.ExampleTarget.intStringMethod",
          "1 + 2" :: "\"world\"" :: Nil
        )
      }

      // This fragment of code does several concurrent evaluations,
      // before checking the final call to ensure there are no race conditions:

      1 to 10 foreach { i ⇒
        val thread = new Thread {
          override def run() = evalCalls()
        }
        thread.start()
      }

      val res = evalCalls()

      res should equal(EvaluationSuccess[String]("3world"))
      assert(res.toSuccessXor.isRight)
      assert(res.toExecutionXor.isRight)
    }

  }
}
