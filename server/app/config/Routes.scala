/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package config

import play.api.mvc.PathBindable.Parsing

object Routes {

  /*
  implicit def CharPathBinder(implicit stringBinder: PathBindable[String]) = new PathBindable[Char] {
    override def bind(key: String, value: String): Either[String, Char] = {
      stringBinder.bind(key,value).fold( err => Left("Invalid ID"), id => Right(id.charAt(0)))
    }

    override def unbind(key: String, value: Char): String = {
      stringBinder.unbind(key, value.toString)
    }
  }
  */
  implicit object bindableChar extends Parsing[Char](
    _.charAt(0), _.toString, (key: String, e: Exception) ⇒ "Cannot parse parameter %s as Char: %s".format(key, e.getMessage)
  )

}
