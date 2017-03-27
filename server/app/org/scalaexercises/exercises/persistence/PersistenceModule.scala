/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence

import doobie.imports._
import shapeless.HNil

import scala.language.higherKinds
import scalaz.Foldable

class PersistenceModule {

  def fetchList[K: Composite](sql: String): ConnectionIO[List[K]] =
    Query[HNil, K](sql).toQuery0(HNil).to[List]

  def fetchList[A: Composite, K: Composite](
      sql: String,
      values: A
  ): ConnectionIO[List[K]] =
    Query[A, K](sql).to[List](values)

  def fetchOption[A: Composite, K: Composite](
      sql: String,
      values: A
  ): ConnectionIO[Option[K]] =
    Query[A, K](sql).option(values)

  def fetchUnique[A: Composite, K: Composite](
      sql: String,
      values: A
  ): ConnectionIO[K] =
    Query[A, K](sql).unique(values)

  def update(sql: String): ConnectionIO[Int] =
    Update[HNil](sql).run(HNil)

  def update[A: Composite](
      sql: String,
      values: A
  ): ConnectionIO[Int] =
    Update[A](sql).run(values)

  def updateWithGeneratedKeys[A: Composite, K: Composite](
      sql: String,
      fields: List[String],
      values: A
  ): ConnectionIO[K] =
    Update[A](sql).withUniqueGeneratedKeys[K](fields: _*)(values)

  def updateMany[F[_]: Foldable, A: Composite](
      sql: String,
      values: F[A]
  ): ConnectionIO[Int] =
    Update[A](sql).updateMany(values)

}

object PersistenceModule {

  implicit def persistenceImpl: PersistenceModule = new PersistenceModule
}
