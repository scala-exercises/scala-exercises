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

package org.scalaexercises.exercises.persistence

import cats.Foldable
import doobie._
import shapeless.HNil

class PersistenceModule {

  def fetchList[K: Read](sql: String): ConnectionIO[List[K]] =
    Query[HNil, K](sql).toQuery0(HNil).to[List]

  def fetchList[A: Write, K: Read](sql: String, values: A): ConnectionIO[List[K]] =
    Query[A, K](sql).to[List](values)

  def fetchOption[A: Write, K: Read](sql: String, values: A): ConnectionIO[Option[K]] =
    Query[A, K](sql).option(values)

  def fetchUnique[A: Write, K: Read](sql: String, values: A): ConnectionIO[K] =
    Query[A, K](sql).unique(values)

  def update(sql: String): ConnectionIO[Int] = Update[HNil](sql).run(HNil)

  def update[A: Write](sql: String, values: A): ConnectionIO[Int] =
    Update[A](sql).run(values)

  def updateWithGeneratedKeys[A: Write, K: Read](
      sql: String,
      fields: List[String],
      values: A
  ): ConnectionIO[K] =
    Update[A](sql).withUniqueGeneratedKeys[K](fields: _*)(values)

  def updateMany[F[_]: Foldable, A: Write](sql: String, values: F[A]): ConnectionIO[Int] =
    Update[A](sql).updateMany(values)

}

object PersistenceModule {

  implicit def persistenceImpl: PersistenceModule = new PersistenceModule
}
