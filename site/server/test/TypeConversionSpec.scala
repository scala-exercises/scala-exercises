import org.scalacheck.{ Gen, Properties }
import org.scalacheck.Prop.{ throws, forAll }

import com.toddfast.util.convert.TypeConverter
import services.exercisev0.BooleanTypeConversion

object BooleanConversionProperties extends Properties("BooleanTypeConversion") {
  def convert(s: String): Boolean = {
    TypeConverter.registerTypeConversion(new BooleanTypeConversion())
    TypeConverter.convert(java.lang.Boolean.TYPE, s)
  }

  val trueGen = for {
    t ← Gen.oneOf("t", "T")
    r ← Gen.oneOf("r", "R")
    u ← Gen.oneOf("u", "U")
    e ← Gen.oneOf("e", "E")
  } yield (t + r + u + e)

  property("`true` strings yield `true` regardless of case") = forAll(trueGen) { t ⇒
    convert(t)
  }

  val falseGen = for {
    f ← Gen.oneOf("f", "F")
    a ← Gen.oneOf("a", "A")
    l ← Gen.oneOf("l", "L")
    s ← Gen.oneOf("s", "S")
    e ← Gen.oneOf("e", "E")
  } yield (f + a + l + s + e)

  property("`false` strings yield `false` regardless of case") = forAll(falseGen) { f ⇒
    !convert(f)
  }

  val trueAndFalse = Set("true", "false")
  val notTrueNorFalseGen = Gen.identifier suchThat { (s: String) ⇒ !trueAndFalse.contains(s.toLowerCase()) }

  property("malformed strings throw an `IllegalArgumentException`") = forAll(notTrueNorFalseGen) { s ⇒
    throws(classOf[IllegalArgumentException])(convert(s))
  }
}
