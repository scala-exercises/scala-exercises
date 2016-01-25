import org.specs2._
import org.specs2.runner._
import org.junit.runner._

import com.toddfast.util.convert.TypeConverter
import services.exercisev0.BooleanTypeConversion

@RunWith(classOf[JUnitRunner])
class TypeConversionSpec extends Specification {
  TypeConverter.registerTypeConversion(new BooleanTypeConversion())

  def is = s2"""
With our custom boolean type conversion

  the `true` string is converted to a truthy boolean $e1
  the `TRUE` string is converted to a truthy boolean $e2
  the `false` string is converted to a truthy boolean $e3
  the `FALSE` string is converted to a truthy boolean $e4
  an empty string is not a legal argument $e5
  a string that isn't `true` is not a truthy boolean $e6
  a string that isn't `false` is not a falsy boolean $e7
"""

  def e1 = TypeConverter.convert(java.lang.Boolean.TYPE, "true") must beEqualTo(true)
  def e2 = TypeConverter.convert(java.lang.Boolean.TYPE, "TRUE") must beEqualTo(true)
  def e3 = TypeConverter.convert(java.lang.Boolean.TYPE, "false") must beEqualTo(false)
  def e4 = TypeConverter.convert(java.lang.Boolean.TYPE, "FALSE") must beEqualTo(false)
  def e5 = TypeConverter.convert(java.lang.Boolean.TYPE, "") must throwA[IllegalArgumentException]
  def e6 = TypeConverter.convert(java.lang.Boolean.TYPE, "tr") must throwA[IllegalArgumentException]
  def e7 = TypeConverter.convert(java.lang.Boolean.TYPE, "fa") must throwA[IllegalArgumentException]
}
