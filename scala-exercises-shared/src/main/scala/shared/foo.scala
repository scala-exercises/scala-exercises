package shared

import java.util.Date

package foo {

case class Foo(
  int: Int = 1,
  string: String = "foo",
  date: Date = new Date,
  listInt: List[Int] = List(1, 2, 3),
  listString: List[String] = List("A", "B", "C"),
  listObj: List[KV] = List(KV("a", "1"), KV("b", "2"))
)

case class KV(k: String, v: String)

}
