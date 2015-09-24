package example
package test

import scala.scalajs.js
import js.Dynamic.{ global => g }
import scala.scalajs.test.JasmineTest

object ScalaJSExampleTest extends JasmineTest {

  describe("ScalaJSExample") {

it("should implement square()") {
import ExampleJS._

expect(square(0)).toBe(0)
expect(square(4)).toBe(16)
expect(square(-5)).toBe(25)
}
}
}
