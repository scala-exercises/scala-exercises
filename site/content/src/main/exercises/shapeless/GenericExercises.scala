package shapelessex

import org.scalatest._
import shapeless._

/** The Isos of earlier shapeless releases have been completely reworked as the new Generic type, 
  * which closely resembles the generic programming capabilities introduced to GHC 7.2.
  * Generic[T], where T is a case class or an abstract type at the root of a case class hierarchy, 
  * maps between values of T and a generic sum of products representation (HLists and Coproducts),
  * 
  * @param name generic
  */
object GenericExercises extends FlatSpec with Matchers with exercise.Section {

  case class Foo(i: Int, s: String, b: Boolean)

  object Helper {
    
    val fooGen = Generic[Foo]

    val foo = Foo(23, "foo", true)
  }

  import Helper._

  /** {{{
    * case class Foo(i: Int, s: String, b: Boolean)
    * 
    * val fooGen = Generic[Foo]
    * 
    * val foo = Foo(23, "foo", true)
    * }}}
    * 
    * We can convert back and forth case class to their HList Generic representation  
    */
  def genericE(res0 : fooGen.Repr, res1 : Int) = {
    val l = fooGen.to(foo)
    l should be (res0)
    val r = 13 :: l.tail
    val newFoo = fooGen.from(r)
    newFoo.i should be (res1)
  }

}
