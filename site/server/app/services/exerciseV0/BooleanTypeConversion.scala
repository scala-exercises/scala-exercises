package services.exercisev0

import com.toddfast.util.convert.TypeConverter

class BooleanTypeConversion extends TypeConverter.Conversion[Boolean] {
  override def getTypeKeys(): Array[Object] = {
    Array(
      java.lang.Boolean.TYPE,
      java.lang.Boolean.TYPE.getClass(),
      java.lang.Boolean.TYPE.getName(),
      TypeConverter.TYPE_BOOLEAN
    )
  }

  override def convert(value: Any): Boolean = {
    val asString = value.toString()
    if (asString.toLowerCase() == "true")
      true
    else if (asString.toLowerCase() == "false")
      false
    else
      throw new IllegalArgumentException(s"Can't convert $asString to a boolean")
  }
}
