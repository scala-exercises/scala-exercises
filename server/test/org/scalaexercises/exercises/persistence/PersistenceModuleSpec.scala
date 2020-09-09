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

import org.scalaexercises.exercises.support.{ArbitraryInstances, DatabaseInstance}
import doobie.implicits._
import doobie._
import org.scalacheck.ScalacheckShapeless._
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import shapeless.HNil
import cats.effect.IO
import cats.implicits._
import com.dimafeng.testcontainers.ForAllTestContainer

trait DatabaseContext extends DatabaseInstance {

  implicit val trx: Transactor[IO]

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
    sql"SELECT name,active,id FROM persistence".query[(String, Boolean, Long)].to[List]

  def fetchItemById(
      id: Long
  ): ConnectionIO[PersistenceItem] =
    sql"SELECT id,name,active FROM persistence WHERE id=$id".query[PersistenceItem].unique

  def fetchItemByStatus(
      active: Boolean
  ): ConnectionIO[List[PersistenceItem]] =
    sql"SELECT id,name,active FROM persistence WHERE active=$active".query[PersistenceItem].to[List]

  def fetchItemByStatuses(
      active: Boolean
  ): ConnectionIO[List[PersistenceItem]] = {
    val inactive = !active
    sql"SELECT id,name,active FROM persistence WHERE active=$active OR active=$inactive"
      .query[PersistenceItem]
      .to[List]
  }

  def deleteAll(): ConnectionIO[Int] =
    Update[HNil]("DELETE FROM persistence").run(HNil)

  val persistenceModule = new PersistenceModule
}

class PersistenceModuleSpec
    extends AnyWordSpec
    with ScalaCheckDrivenPropertyChecks
    with Matchers
    with BeforeAndAfterEach
    with ForAllTestContainer
    with DatabaseContext
    with ArbitraryInstances
    with DatabaseInstance {

  override lazy val trx: Transactor[IO] = databaseTransactor

  override def beforeEach: Unit = {
    for {
      _ <- dropTable
      _ <- createTable
    } yield ()
  }.transact(trx).unsafeRunSync()

  "fetchList (SQL without parameters)" should {
    "return an empty list if the table is empty" in {

      forAll { user: PersistenceItem =>
        val list = persistenceModule
          .fetchList[PersistenceItem](
            sql = fetchAllSql
          )
          .transact(trx)
          .unsafeRunSync()

        list shouldBe empty
      }
    }

    "return a list of PersistenceItem ((String, Boolean) tuple list) if there are some elements in the table " +
      "that meet the criteria" in {

        forAll { data: List[(String, Boolean)] =>
          deleteAll().transact(trx).unsafeRunSync()
          insertItems(data).transact(trx).unsafeRunSync()

          val list = persistenceModule
            .fetchList[PersistenceItem](
              sql = fetchAllSql
            )
            .transact(trx)
            .unsafeRunSync()

          list.size shouldBe data.size
        }
      }
  }

  "return an empty list of PersistenceItem (String list) if there are some elements in the table " +
    "that meet the criteria" in {

      forAll { names: List[String] =>
        val namesWithStatus = names map ((_, false))
        insertItems(namesWithStatus).transact(trx).unsafeRunSync()

        val list = persistenceModule
          .fetchList[PersistenceItem](
            sql = fetchAllActiveSql
          )
          .transact(trx)
          .unsafeRunSync()

        list shouldBe empty
      }
    }

  "fetchList" should {
    "return an empty list if the table is empty" in {

      forAll { status: Boolean =>
        val list = persistenceModule
          .fetchList[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = status
          )
          .transact(trx)
          .unsafeRunSync()

        list shouldBe empty
      }

    }
  }
  "return a list of PersistenceItem if there are some elements in the table that meet the criteria" in {

    forAll { names: List[String] =>
      deleteAll().transact(trx).unsafeRunSync()
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).unsafeRunSync()

      val list: List[PersistenceItem] = persistenceModule
        .fetchList[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = true
        )
        .transact(trx)
        .unsafeRunSync()

      list.size shouldBe names.size
      list.forall(item => item.active) shouldBe true
    }
  }
  "return an empty list if there aren't any elements in the table that meet the criteria" in {

    forAll { names: List[String] =>
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).unsafeRunSync()

      val list: List[PersistenceItem] = persistenceModule
        .fetchList[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = false
        )
        .transact(trx)
        .unsafeRunSync()

      list shouldBe empty
    }
  }

  "fetchOption" should {
    "return None if the table is empty" in {

      forAll { status: Boolean =>
        val persistenceItem = persistenceModule
          .fetchOption[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = status
          )
          .transact(trx)
          .unsafeRunSync()

        persistenceItem shouldBe empty
      }
    }
    "return a PersistenceItem if there is an element in the table that meets the criteria" in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafeRunSync()

        val persistenceItem = persistenceModule
          .fetchOption[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, active)
          )
          .transact(trx)
          .unsafeRunSync()

        persistenceItem shouldBe Some(PersistenceItem(id, name, active))
      }
    }
    "return None if there isn't any element in the table that meets the criteria" in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafeRunSync()
        val persistenceItem = persistenceModule
          .fetchOption[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, !active)
          )
          .transact(trx)
          .unsafeRunSync()

        persistenceItem shouldBe empty
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(genBoundedList(minSize = 2, gen = Gen.alphaStr)) { names: List[String] =>
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafeRunSync()

        an[Throwable] should be thrownBy persistenceModule
          .fetchOption[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = true
          )
          .transact(trx)
          .unsafeRunSync()

      }
    }
  }

  "fetchUnique" should {
    "throw an exception if the table is empty" in {

      forAll { id: Long =>
        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[Long, PersistenceItem](
            sql = fetchByIdSql,
            values = id
          )
          .transact(trx)
          .unsafeRunSync()
      }
    }
    "return a PersistenceItem if there is an element in the table with the given id" in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafeRunSync()
        val item = persistenceModule
          .fetchUnique[Long, PersistenceItem](
            sql = fetchByIdSql,
            values = id
          )
          .transact(trx)
          .unsafeRunSync()

        item.id shouldBe id
        item.name shouldBe name
      }
    }
    "throw an exception if there isn't any element in the table that meet the criteria" in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafeRunSync()

        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[(Long, Boolean), PersistenceItem](
            sql = fetchByIdAndStatusSql,
            values = (id, !active)
          )
          .transact(trx)
          .unsafeRunSync()
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(genBoundedList(minSize = 2, gen = Gen.alphaStr)) { names: List[String] =>
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).unsafeRunSync()

        an[Throwable] should be thrownBy persistenceModule
          .fetchUnique[Boolean, PersistenceItem](
            sql = fetchByStatusSql,
            values = true
          )
          .transact(trx)
          .unsafeRunSync()
      }
    }
  }

  "update (SQL without parameters)" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

        forAll { _: Int =>
          persistenceModule
            .update(sql = updateAllSql)
            .transact(trx)
            .unsafeRunSync() shouldBe 0
        }
      }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

        forAll { names: List[String] =>
          val namesWithStatus = names map ((_, true))
          insertItems(namesWithStatus).transact(trx).unsafeRunSync()

          val updateResult = persistenceModule
            .update(updateAllActiveSql)
            .transact(trx)
            .unsafeRunSync()

          updateResult shouldBe names.size
        }
      }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

        forAll { names: List[String] =>
          val namesWithStatus = names map ((_, false))
          insertItems(namesWithStatus).transact(trx).unsafeRunSync()

          persistenceModule
            .update(updateAllActiveSql)
            .transact(trx)
            .unsafeRunSync() shouldBe 0
        }
      }
  }

  "update" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

        forAll { active: Boolean =>
          persistenceModule
            .update(
              sql = updateByStatusSql,
              values = (!active, active)
            )
            .transact(trx)
            .unsafeRunSync() shouldBe 0
        }
      }
    "return the number of affected rows equals to 1 after updating a single item in the table " in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data
        val id             = insertItem(name = name, active = active).transact(trx).unsafeRunSync()

        persistenceModule
          .update(
            sql = updateByIdSql,
            values = (name, !active, id)
          )
          .transact(trx)
          .unsafeRunSync() shouldBe 1

        fetchItemById(id).transact(trx).unsafeRunSync() shouldBe PersistenceItem(id, name, !active)
      }
    }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

        forAll { names: List[String] =>
          val namesWithStatus = names map ((_, true))
          insertItems(namesWithStatus).transact(trx).unsafeRunSync()

          val updateResult =
            persistenceModule
              .update(
                sql = updateByStatusSql,
                values = (false, true)
              )
              .transact(trx)
              .unsafeRunSync()

          updateResult shouldBe names.size
        }
      }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

        forAll { names: List[String] =>
          val namesWithStatus = names map ((_, false))
          insertItems(namesWithStatus).transact(trx).unsafeRunSync()

          persistenceModule
            .update(
              sql = updateByStatusSql,
              values = (false, true)
            )
            .transact(trx)
            .unsafeRunSync() shouldBe 0
        }
      }
    "return the number of affected rows equals to 1 after inserting a new item in the table" in {

      forAll { data: (String, Boolean) =>
        persistenceModule
          .update(
            sql = insertSql,
            values = (data._1, data._2)
          )
          .transact(trx)
          .unsafeRunSync() shouldBe 1
      }
    }
  }

  "updateWithGeneratedKeys" should {
    "insert a new PersistenceItem into the table" in {

      forAll { data: (String, Boolean) =>
        val (name, active) = data

        val updateResult = persistenceModule
          .updateWithGeneratedKeys[(String, Boolean), Long](
            sql = insertSql,
            fields = List("id"),
            values = data
          )
          .transact(trx)
          .unsafeRunSync()

        fetchItemById(updateResult).transact(trx).unsafeRunSync() shouldBe PersistenceItem(
          updateResult,
          name,
          active
        )
      }
    }
  }

  "updateMany" should {
    "return the number of affected rows after inserting a batch of items in the table" in {

      forAll { data: List[(String, Boolean)] =>
        persistenceModule
          .updateMany[List, (String, Boolean)](
            sql = insertSql,
            values = data
          )
          .transact(trx)
          .unsafeRunSync() shouldBe data.size
      }
    }

    "return the number of affected rows equals to 0 after updating a batch of items " +
      "in the table if the table is empty" in {

        forAll { data: List[(String, Boolean, Long)] =>
          persistenceModule
            .updateMany[List, (String, Boolean, Long)](
              sql = updateByIdSql,
              values = data
            )
            .transact(trx)
            .unsafeRunSync() shouldBe 0
        }
      }
    "return the number of affected rows after updating a batch of items in the table " +
      "if the items exist" in {

        forAll { data: List[(String, Boolean)] =>
          val fetchData = {
            for {
              _      <- insertItems(data)
              result <- fetchAll
            } yield result
          }.transact(trx).unsafeRunSync() map { case (name, active, id) =>
            (name, !active, id)
          }

          persistenceModule
            .updateMany[List, (String, Boolean, Long)](
              sql = updateByIdSql,
              values = fetchData
            )
            .transact(trx)
            .unsafeRunSync() shouldBe fetchData.size
        }
      }
  }
}
