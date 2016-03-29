package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

/**
 * == Singleton-typed Symbols ==
 *
 * Scala's `Symbol` type, despite having its own syntax and being isomorphic to the `String` type, isn't equipped with
 * useful singleton-typed literals. An encoding of singleton types for `Symbol` literals has proven to valuable (see
 * below), and is represented by tagging the non-singleton type with the singleton type of the corresponding `String`
 * literal
 *
 * @param name singleton_typed_symbols
 */
object SingletonTypedSymbolsExercises extends FlatSpec with Matchers with exercise.Section {

  /*
  def taggedSymbols(res0 : Any) = {
    import syntax.singleton._
    val s : Symbol with shapeless.tag.Tagged[String("foo")] = res0
  }
   */

}

