/*
 *  scala-exercises
 *
 *  Copyright 2015-2019 47 Degrees, LLC. <http://www.47deg.com>
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
 *
 */

package org.scalaexercises.content

import org.scalaexercises.runtime.model.Library

object LibraryA extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

object LibraryB extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

object LibraryC extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}

class ErrorLibrary extends Library {
  override def owner         = ???
  override def repository    = ???
  override def color         = ???
  override def logoPath      = ???
  override def logoData      = ???
  override def description   = ???
  override def name          = ???
  override def sections      = ???
  override def timestamp     = ???
  override def buildMetaInfo = ???
}
