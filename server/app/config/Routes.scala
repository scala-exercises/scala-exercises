/*
 * Copyright 2014-2020 47 Degrees Open Source <https://www.47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
  implicit object bindableChar
      extends Parsing[Char](
        _.charAt(0),
        _.toString,
        (key: String, e: Exception) =>
          "Cannot parse parameter %s as Char: %s".format(key, e.getMessage)
      )

}
