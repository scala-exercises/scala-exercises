/*
 *  scala-exercises
 *
 *  Copyright 2015-2017 47 Degrees, LLC. <http://www.47deg.com>
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

package org.scalaexercises.exercises.persistence

import org.scalaexercises.exercises.support.{ArbitraryInstances, DatabaseInstance}
import doobie.imports._
import doobie.util.iolite
import org.scalacheck.Shapeless._
import org.scalacheck.Gen
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import shapeless.HNil

import _root_.scalaz.std.iterable._

trait DatabaseContext extends DatabaseInstance {

  implicit val trx: Transactor[iolite.IOLite] = databaseTransactor

  case class PersistenceItem(id: Long, name: String, active: Boolean)

  val fetchAllSql           = "SELECT id,name,active FROM persistence"
  val fetchAllActiveSql     = "SELECT id,name,active FROM persistence WHERE active=true"
  val fetchByIdSql          = "SELECT id,name,active FROM persistence WHERE id=?"
  val fetchByIdAndStatusSql = "SELECT id,name,active FROM persistence WHERE id=? AND active=?"
  val fetchByStatusSql      = "SELECT id,name,active FROM persistence WHERE active=?"
  val insertSql             = "INSERT INTO persistence (name,active) VALUES (?,?)"
  val updateAllSql          = "UPDATE persistence SET active=false"
  val updateAllActiveSql    = "UPDATE persistence SET active=false WHERE active=true"
  val updateByIdSql         = "UPDATE persistence SET name=?,active=? WHERE id=?"
  val updateByStatusSql     = "UPDATE persistence SET active=? WHERE active=?"

  def createTable: ConnectionIO[Int] =
    sql"""
          CREATE TABLE persistence (
          id   BIGSERIAL PRIMARY KEY,
          name VARCHAR NOT NULL,
          active BOOL NOT NULL)""".update.run

  def dropTable: ConnectionIO[Int] = sql"""DROP TABLE IF EXISTS persistence""".update.run

  def insertItem(
      name: String,
      active: Boolean
  ): ConnectionIO[Long] =
    sql"INSERT INTO persistence (name, active) VALUES ($name,$active)".update
      .withUniqueGeneratedKeys[Long]("id")

  def insertItems(
      values: List[(String, Boolean)]
  ): ConnectionIO[Int] =
    Update[(String, Boolean)]("INSERT INTO persistence (name, active) VALUES (?,?)")
      .updateMany(values)

  def fetchAll: ConnectionIO[List[(String, Boolean, Long)]] =
    sql"SELECT name,active,id FROM persistence".query[(String, Boolean, Long)].list

  def fetchItemById(
      id: Long
  ): ConnectionIO[PersistenceItem] =
    sql"SELECT id,name,active FROM persistence WHERE id=$id".query[PersistenceItem].unique

  def fetchItemByStatus(
      active: Boolean
  ): ConnectionIO[List[PersistenceItem]] =
    sql"SELECT id,name,active FROM persistence WHERE active=$active".query[PersistenceItem].list

  def fetchItemByStatuses(
      active: Boolean
  ): ConnectionIO[List[PersistenceItem]] = {
    val inactive = !active
    sql"SELECT id,name,active FROM persistence WHERE active=$active OR active=$inactive"
      .query[PersistenceItem]
      .list
  }

  def deleteAll(): ConnectionIO[Int] =
    Update[HNil]("DELETE FROM persistence").run(HNil)

  val persistenceModule = new PersistenceModule
}

class PersistenceModuleSpec
    extends WordSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with BeforeAndAfterEach
    with DatabaseContext
    with ArbitraryInstances
    with DatabaseInstance {

  override def beforeEach = {
    for {
      _ ← dropTable
      _ ← createTable
    } yield ()
  }.transact(trx).unsafePerformIO

  "fetchList (SQL without parameters)" should {
    "return an empty list if the table is empty" in {

      forAll { user: PersistenceItem ⇒
        val list = persistenceModule
          .fetchList[PersistenceItem](
            sql = fetchAllSql
          )
          .transact(trx)
          .unsafePerformIO

        list shouldBe empty
      }
    }

    "return a list of PersistenceItem ((String, Boolean) tuple list) if there are some elements in the table " +
      "that meet the criteria" in {

      forAll { (data: List[(String, Boolean)]) ⇒
        deleteAll().transact(trx).unsafePerformIO
        insertItems(data).transact(trx).unsafePerformIO

        val list = persistenceModule
          .fetchList[PersistenceItem](
            sql = fetchAllSql
          )
          .transact(trx)
          .unsafePerformIO

        list.size shouldBe data.size
      }
    }
  }

  "return an empty list of PersistenceItem (String list) if there are some elements in the table " +
    "that meet the criteria" in {

    forAll { (names: List[String]) ⇒
      val namesWithStatus = names map ((_, false))
      insertItems(namesWithStatus).transact(trx).unsafePerformIO

      val list = persistenceModule
        .fetchList[PersistenceItem](
          sql = fetchAllActiveSql
        )
        .transact(trx)
        .unsafePerformIO

      list shouldBe empty
    }
  }

  "fetchList" should {
    "return an empty list if the table is empty" in {

      forAll { (status: Boolean) ⇒
        val list = persistenceModule
          .fetchList[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = status
          )
          .transact(trx)
          .unsafePerformIO

        list shouldBe empty
      }

    }
  }
  "return a list of PersistenceItem if there are some elements in the table that meet the criteria" in {

    forAll { (names: List[String]) ⇒
      deleteAll().transact(trx).unsafePerformIO
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).unsafePerformIO

      val list: List[PersistenceItem] = persistenceModule
        .fetchList[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = true
        )
        .transact(trx)
        .unsafePerformIO

      list.size shouldBe names.size
      list.forall(item ⇒ item.active) shouldBe true
    }
  }
  "return an empty list if there aren't any elements in the table that meet the criteria" in {

    forAll { (names: List[String]) ⇒
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).unsafePerformIO

      val list: List[PersistenceItem] = persistenceModule
        .fetchList[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = false
        )
        .transact(trx)
        .unsafePerformIO

      list shouldBe empty
    }
  }

  "fetchOption" should {
    "return None if the table is empty" in {

      forAll { (status: Boolean) ⇒
        val persistenceItem = persistenceModule
          .fetchOption[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = status
          )
          .transact(trx)
          .unsafePerformIO

        persistenceItem shouldBe empty
      }
    }
    "return a PersistenceItem if there is an element in the table that meets the criteria" in {

      forAll { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafePerformIO

        val persistenceItem = persistenceModule
          .fetchOption[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, active)
          )
          .transact(trx)
          .unsafePerformIO

        persistenceItem shouldBe Some(PersistenceItem(id, name, active))
      }
    }
    "return None if there isn't any element in the table that meets the criteria" in {

      forAll { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafePerformIO
        val persistenceItem = persistenceModule
          .fetchOption[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, !active)
          )
          .transact(trx)
          .unsafePerformIO

        persistenceItem shouldBe empty
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(genBoundedList(minSize = 2, gen = Gen.alphaStr)) { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        an[Throwable] should be thrownBy persistenceModule
          .fetchOption[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = true
          )
          .transact(trx)
          .unsafePerformIO

      }
    }
  }

  "fetchUnique" should {
    "throw an exception if the table is empty" in {

      forAll { (id: Long) ⇒
        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[Long, PersistenceItem](
            sql = fetchByIdSql,
            values = id
          )
          .transact(trx)
          .unsafePerformIO
      }
    }
    "return a PersistenceItem if there is an element in the table with the given id" in {

      forAll { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafePerformIO
        val item = persistenceModule
          .fetchUnique[Long, PersistenceItem](
            sql = fetchByIdSql,
            values = id
          )
          .transact(trx)
          .unsafePerformIO

        item.id shouldBe id
        item.name shouldBe name
      }
    }
    "throw an exception if there isn't any element in the table that meet the criteria" in {

      forAll { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafePerformIO

        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, !active)
          )
          .transact(trx)
          .unsafePerformIO
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(genBoundedList(minSize = 2, gen = Gen.alphaStr)) { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = true
          )
          .transact(trx)
          .unsafePerformIO
      }
    }
  }

  "update (SQL without parameters)" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

      forAll { (_: Int) ⇒
        persistenceModule
          .update(sql = updateAllSql)
          .transact(trx)
          .unsafePerformIO shouldBe 0
      }
    }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

      forAll { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        val updateResult = persistenceModule
          .update(updateAllActiveSql)
          .transact(trx)
          .unsafePerformIO

        updateResult shouldBe names.size
      }
    }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

      forAll { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, false))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        persistenceModule
          .update(updateAllActiveSql)
          .transact(trx)
          .unsafePerformIO shouldBe 0
      }
    }
  }

  "update" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

      forAll { (active: Boolean) ⇒
        persistenceModule
          .update(
            sql = updateByStatusSql,
            values = (!active, active)
          )
          .transact(trx)
          .unsafePerformIO shouldBe 0
      }
    }
    "return the number of affected rows equals to 1 after updating a single item in the table " in {

      forAll { data: (String, Boolean) ⇒
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafePerformIO

        persistenceModule
          .update(
            sql = updateByIdSql,
            values = (name, !active, id)
          )
          .transact(trx)
          .unsafePerformIO shouldBe 1

        fetchItemById(id).transact(trx).unsafePerformIO shouldBe PersistenceItem(id, name, !active)
      }
    }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

      forAll { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        val updateResult =
          persistenceModule
            .update(
              sql = updateByStatusSql,
              values = (false, true)
            )
            .transact(trx)
            .unsafePerformIO

        updateResult shouldBe names.size
      }
    }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

      forAll { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, false))
        insertItems(namesWithStatus).transact(trx).unsafePerformIO

        persistenceModule
          .update(
            sql = updateByStatusSql,
            values = (false, true)
          )
          .transact(trx)
          .unsafePerformIO shouldBe 0
      }
    }
    "return the number of affected rows equals to 1 after inserting a new item in the table" in {

      forAll { data: (String, Boolean) ⇒
        persistenceModule
          .update(
            sql = insertSql,
            values = (data._1, data._2)
          )
          .transact(trx)
          .unsafePerformIO shouldBe 1
      }
    }
  }

  "updateWithGeneratedKeys" should {
    "insert a new PersistenceItem into the table" in {

      forAll { data: (String, Boolean) ⇒
        val (name, active) = data

        val updateResult = persistenceModule
          .updateWithGeneratedKeys[(String, Boolean), Long](
            sql = insertSql,
            fields = List("id"),
            values = data
          )
          .transact(trx)
          .unsafePerformIO

        fetchItemById(updateResult).transact(trx).unsafePerformIO shouldBe PersistenceItem(
          updateResult,
          name,
          active)
      }
    }
  }

  "updateMany" should {
    "return the number of affected rows after inserting a batch of items in the table" in {

      forAll { (data: List[(String, Boolean)]) ⇒
        persistenceModule
          .updateMany[List, (String, Boolean)](
            sql = insertSql,
            values = data
          )
          .transact(trx)
          .unsafePerformIO shouldBe data.size
      }
    }

    "return the number of affected rows equals to 0 after updating a batch of items " +
      "in the table if the table is empty" in {

      forAll { (data: List[(String, Boolean, Long)]) ⇒
        persistenceModule
          .updateMany[List, (String, Boolean, Long)](
            sql = updateByIdSql,
            values = data
          )
          .transact(trx)
          .unsafePerformIO shouldBe 0
      }
    }
    "return the number of affected rows after updating a batch of items in the table " +
      "if the items exist" in {

      forAll { (data: List[(String, Boolean)]) ⇒
        val fetchData = {
          for {
            _      ← insertItems(data)
            result ← fetchAll
          } yield result
        }.transact(trx).unsafePerformIO map {
          case (name, active, id) ⇒ (name, !active, id)
        }

        persistenceModule
          .updateMany[List, (String, Boolean, Long)](
            sql = updateByIdSql,
            values = fetchData
          )
          .transact(trx)
          .unsafePerformIO shouldBe fetchData.size
      }
    }
  }
}
