package shapelessex

import org.scalatest._
import shapeless._
import ops.hlist._

/** == Extensible records ==
  *
  * shapeless provides an implementation of extensible records modelled as `HLists` of values tagged with the singleton
  * types of their keys. This means that there is no concrete representation needed at all for the keys. Amongst other
  * things this will allow subsequent work on `Generic` to map case classes directly to records with their member names
  * encoded in their element types.
  * {{{
  * import shapeless._ ; import syntax.singleton._ ; import record._
  *
  * val book =
  * ("author" ->> "Benjamin Pierce") ::
  * ("title"  ->> "Types and Programming Languages") ::
  * ("id"     ->>  262162091) ::
  * ("price"  ->>  44.11) ::
  * HNil
  * }}}
  *
  * @param name extensible_records
  *
  */
object ExtensibleRecordsExercises extends FlatSpec with Matchers with exercise.Section {

  import shapeless._; import syntax.singleton._; import record._

  val book =
    ("author" ->> "Benjamin Pierce") ::
      ("title" ->> "Types and Programming Languages") ::
      ("id" ->> 262162091) ::
      ("price" ->> 44.11) ::
      HNil

  /**
    */
  def resultTypes(res0: String, res1: String, res2: Int, res3: Double) = {
    book("author") should be(res0)
    book("title") should be(res1)
    book("id") should be(res2)
    book("price") should be(res3)
  }

  /*
  /** Keys are materialized from singleton types encoded in value type
    */
  def keys(res0 : ?) = {
    book.keys should be (res0)
  }
   */
  /** values
    */
  def values(res0: String :: String :: Int :: Double :: HNil) = {
    book.values should be(res0)
  }

  /** Update, Add or remove a field
    */
  def updated(res0: Double, res1: Boolean, res2: String :: String :: Double :: Boolean :: HNil) = {
    val newPrice = book("price") + 2.0
    val updated = book + ("price" ->> newPrice)

    updated("price") should be(res0)

    val extended = updated + ("inPrint" ->> true)

    extended("inPrint") should be(res1)

    val noId = extended - "id"

    noId.values should be(res2)

    //noId("id")  // Attempting to access a missing field is a compile time error
    // error: could not find implicit value for parameter selector ...
    //          noId("id")
  }

}
