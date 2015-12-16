package example

import utest._

object ScalaJSExampleSpec extends TestSuite {

  import ExampleJS._

  def tests = TestSuite {
    'ScalaJSExample{
      assert(square(0) == 0)
      assert(square(4) == 16)
      assert(square(-5) == 25)
    }
  }

}