/*
 * scala-exercises-server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

import com.fortysevendeg.exercises.persistence.PersistenceModule
import doobie.imports._
import org.scalacheck.{ Arbitrary, Gen }
import org.scalacheck.Shapeless._
import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import _root_.scalaz.concurrent.Task
import _root_.scalaz.std.iterable._
import scalaz.{ -\/, \/- }

trait DatabaseContext {

  def trx = DriverManagerTransactor[Task]("org.h2.Driver", "jdbc:h2:mem:test3;DB_CLOSE_DELAY=-1", "sa", "")

  case class PersistenceItem(id: Long, name: String, active: Boolean)

  val fetchAllSql = "SELECT id,name,active FROM persistence"
  val fetchAllActiveSql = "SELECT id,name,active FROM persistence WHERE active=true"
  val fetchByIdSql = "SELECT id,name,active FROM persistence WHERE id=?"
  val fetchByIdAndStatusSql = "SELECT id,name,active FROM persistence WHERE id=? AND active=?"
  val fetchByStatusSql = "SELECT id,name,active FROM persistence WHERE active=?"
  val insertSql = "INSERT INTO persistence (name,active) VALUES (?,?)"
  val updateAllSql = "UPDATE persistence SET active=false"
  val updateAllActiveSql = "UPDATE persistence SET active=false WHERE active=true"
  val updateByIdSql = "UPDATE persistence SET name=?,active=? WHERE id=?"
  val updateByStatusSql = "UPDATE persistence SET active=? WHERE active=?"

  def createTable: ConnectionIO[Int] =
    sql"""
          CREATE TABLE persistence (
          id   BIGINT AUTO_INCREMENT,
          name VARCHAR NOT NULL,
          active BOOL NOT NULL)""".update.run

  def dropTable: ConnectionIO[Int] = sql"""DROP TABLE IF EXISTS persistence""".update.run

  def insertItem(
    name:   String,
    active: Boolean
  ): ConnectionIO[Long] =
    sql"INSERT INTO persistence (name, active) VALUES ($name,$active)".update.withUniqueGeneratedKeys[Long]("id")

  def insertItems(
    values: List[(String, Boolean)]
  ): ConnectionIO[Int] =
    Update[(String, Boolean)]("INSERT INTO persistence (name, active) VALUES (?,?)").updateMany(values)

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
    sql"SELECT id,name,active FROM persistence WHERE active=$active OR active=$inactive".query[PersistenceItem].list
  }

  val persistenceModule = new PersistenceModule

  def genBoundedList[T](minSize: Int = 1, maxSize: Int = 100, gen: Gen[T]): Gen[List[T]] =
    Gen.choose(minSize, maxSize) flatMap { size ⇒ Gen.listOfN(size, gen) }

  implicit val dataWithId: Arbitrary[List[(Long, String, Boolean)]] =
    Arbitrary(
      genBoundedList(
        minSize = 2,
        maxSize = 10,
        gen = Gen.resultOf((l: Long, s: String, b: Boolean) ⇒ (l, s, b))
      )
    )

  implicit val dataStringBoolean: Arbitrary[List[(String, Boolean)]] =
    Arbitrary(genBoundedList(
      minSize = 2,
      maxSize = 10,
      gen = Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))
    ))

  implicit val dataStringBooleanLong: Arbitrary[List[(String, Boolean, Long)]] =
    Arbitrary(genBoundedList(
      minSize = 2,
      maxSize = 10,
      gen = Gen.resultOf((s: String, b: Boolean, l: Long) ⇒ (s, b, l))
    ))

  implicit val stringList: Arbitrary[List[String]] =
    Arbitrary(genBoundedList(minSize = 2, gen = Gen.resultOf((s: String) ⇒ s)))

  implicitly[Arbitrary[PersistenceItem]]

  val persistenceItemGen = for {
    pItem ← Arbitrary.arbitrary[PersistenceItem]
    identifier ← Gen.identifier
  } yield pItem.copy(name = identifier)
}

class PersistenceModuleSpec
    extends WordSpec
    with GeneratorDrivenPropertyChecks
    with Matchers
    with BeforeAndAfterEach
    with DatabaseContext {

  override def beforeEach = {
    for {
      _ ← dropTable
      _ ← createTable
    } yield ()
  }.transact(trx).run

  "fetchList (SQL without parameters)" should {
    "return an empty list if the table is empty" in {

      forAll(persistenceItemGen) { user ⇒
        val list = persistenceModule.fetchList[PersistenceItem](
          sql = fetchAllSql
        ).transact(trx).run

        list shouldBe empty
      }
    }
  }

  "return a list of PersistenceItem ((String, Boolean) tuple list) if there are some elements in the table " +
    "that meet the criteria" in {

      forAll(dataStringBoolean.arbitrary) { (data: List[(String, Boolean)]) ⇒
        insertItems(data).transact(trx).run

        val list = persistenceModule.fetchList[PersistenceItem](
          sql = fetchAllSql
        ).transact(trx).run

        list should not be empty
      }
    }

  "return a list of PersistenceItem (String list) if there are some elements in the table " +
    "that meet the criteria" in {

      forAll(stringList.arbitrary) { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, false))
        insertItems(namesWithStatus).transact(trx).run

        val list = persistenceModule.fetchList[PersistenceItem](
          sql = fetchAllActiveSql
        ).transact(trx).run

        list shouldBe empty
      }
    }

  "fetchList" should {
    "return an empty list if the table is empty" in {

      forAll(Gen.oneOf(true, false)) { (status: Boolean) ⇒
        val list = persistenceModule.fetchList[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = status
        ).transact(trx).run

        list shouldBe empty
      }

    }
  }
  "return a list of PersistenceItem if there are some elements in the table that meet the criteria" in {

    forAll(stringList.arbitrary) { (names: List[String]) ⇒
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).run

      val list: List[PersistenceItem] = persistenceModule.fetchList[Boolean, PersistenceItem](
        sql = fetchByStatusSql,
        values = true
      ).transact(trx).run

      list should not be empty
      list.forall(item ⇒ item.active) shouldBe true
    }
  }
  "return an empty list if there aren't any elements in the table that meet the criteria" in {

    forAll(stringList.arbitrary) { (names: List[String]) ⇒
      val namesWithStatus = names map ((_, true))
      insertItems(namesWithStatus).transact(trx).run

      val list: List[PersistenceItem] = persistenceModule.fetchList[Boolean, PersistenceItem](
        sql = fetchByStatusSql,
        values = false
      ).transact(trx).run

      list shouldBe empty
    }
  }

  "fetchOption" should {
    "return None if the table is empty" in {

      forAll(Gen.oneOf(true, false)) { (status: Boolean) ⇒
        val persistenceItem = persistenceModule.fetchOption[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = status
        ).transact(trx).run

        persistenceItem shouldBe empty
      }
    }
    "return a PersistenceItem if there is an element in the table that meets the criteria" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id = insertItem(name = name, active = active).transact(trx).run

        val persistenceItem = persistenceModule.fetchOption[(Long, Boolean), PersistenceItem](
          sql = fetchByIdAndStatusSql,
          values = (id, active)
        ).transact(trx).run

        persistenceItem shouldBe Some(PersistenceItem(id, name, active))
      }
    }
    "return None if there isn't any element in the table that meets the criteria" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id = insertItem(name = name, active = active).transact(trx).run
        val persistenceItem = persistenceModule.fetchOption[(Long, Boolean), PersistenceItem](
          sql = fetchByIdAndStatusSql,
          values = (id, !active)
        ).transact(trx).run

        persistenceItem shouldBe empty
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(stringList.arbitrary) { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).run

        an[Throwable] should be thrownBy persistenceModule.fetchOption[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = true
        ).transact(trx).run

      }
    }
  }

  "fetchUnique" should {
    "throw an exception if the table is empty" in {

      forAll(Gen.choose[Long](1, 100)) { (id: Long) ⇒
        persistenceModule.fetchUnique[Long, PersistenceItem](
          sql = fetchByIdSql,
          values = id
        ).transact(trx).attemptRun shouldBe a[-\/[Throwable]]
      }
    }
    "return a PersistenceItem if there is an element in the table with the given id" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id = insertItem(name = name, active = active).transact(trx).run
        val item = persistenceModule.fetchUnique[Long, PersistenceItem](
          sql = fetchByIdSql,
          values = id
        ).transact(trx).run

        item.id shouldBe id
        item.name shouldBe name
      }
    }
    "throw an exception if there isn't any element in the table that meet the criteria" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { (data: (String, Boolean)) ⇒
        val (name, active) = data
        val id = insertItem(name = name, active = active).transact(trx).run

        persistenceModule.fetchUnique[(Long, Boolean), PersistenceItem](
          sql = fetchByIdAndStatusSql,
          values = (id, !active)
        ).transact(trx).attemptRun shouldBe a[-\/[Throwable]]
      }
    }
    "throw an exception if there are more than one element in the table that meet the criteria" in {

      forAll(stringList.arbitrary) { (names: List[String]) ⇒
        val namesWithStatus = names map ((_, true))
        insertItems(namesWithStatus).transact(trx).run

        an[Throwable] should be thrownBy persistenceModule.fetchUnique[Boolean, PersistenceItem](
          sql = fetchByStatusSql,
          values = true
        ).transact(trx).run
      }
    }
  }

  "update (SQL without parameters)" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

        forAll(Gen.chooseNum[Int](1, 100)) { (i: Int) ⇒
          persistenceModule.update(sql = updateAllSql).transact(trx).attemptRun shouldBe \/-[Int](0)
        }
      }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

        forAll(stringList.arbitrary) { (names: List[String]) ⇒
          val namesWithStatus = names map ((_, true))
          insertItems(namesWithStatus).transact(trx).run

          val updateResult = persistenceModule.update(updateAllActiveSql).transact(trx).attemptRun

          updateResult shouldBe a[\/-[Int]]
          updateResult match {
            case \/-(affectedRows: Int) ⇒ affectedRows should be > 0
          }
        }
      }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

        forAll(stringList.arbitrary) { (names: List[String]) ⇒
          val namesWithStatus = names map ((_, false))
          insertItems(namesWithStatus).transact(trx).run

          persistenceModule.update(updateAllActiveSql).transact(trx).attemptRun shouldBe \/-[Int](0)
        }
      }
  }

  "update" should {
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if the table is empty" in {

        forAll(Gen.oneOf(true, false)) { (active: Boolean) ⇒
          persistenceModule.update(
            sql = updateByStatusSql,
            values = (!active, active)
          ).transact(trx).attemptRun shouldBe \/-[Int](0)
        }
      }
    "return the number of affected rows equals to 1 after updating a single item in the table " in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { data: (String, Boolean) ⇒
        val (name, active) = data
        val id = insertItem(name = name, active = active).transact(trx).run

        persistenceModule.update(
          sql = updateByIdSql,
          values = (name, !active, id)
        ).transact(trx).attemptRun shouldBe \/-[Int](1)

        fetchItemById(id).transact(trx).attemptRun shouldBe \/-[PersistenceItem](PersistenceItem(id, name, !active))
      }
    }
    "return the number of affected rows after updating items in the table " +
      "if there are some elements that meet the criteria" in {

        forAll(stringList.arbitrary) { (names: List[String]) ⇒
          val namesWithStatus = names map ((_, true))
          insertItems(namesWithStatus).transact(trx).run

          val updateResult =
            persistenceModule.update(
              sql = updateByStatusSql,
              values = (false, true)
            ).transact(trx).attemptRun

          updateResult shouldBe a[\/-[Int]]
          updateResult match {
            case \/-(affectedRows: Int) ⇒ affectedRows should be > 0
          }
        }
      }
    "return the number of affected rows equals to 0 after updating items in the table " +
      "if there aren't any elements that meet the criteria" in {

        forAll(stringList.arbitrary) { (names: List[String]) ⇒
          val namesWithStatus = names map ((_, false))
          insertItems(namesWithStatus).transact(trx).run

          persistenceModule.update(
            sql = updateByStatusSql,
            values = (false, true)
          ).transact(trx).attemptRun shouldBe \/-[Int](0)
        }
      }
    "return the number of affected rows equals to 1 after inserting a new item in the table" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { data: (String, Boolean) ⇒

        persistenceModule.update(
          sql = insertSql,
          values = (data._1, data._2)
        ).transact(trx).attemptRun shouldBe \/-[Int](1)
      }
    }
  }

  "updateWithGeneratedKeys" should {
    "insert a new PersistenceItem into the table" in {

      forAll(Gen.resultOf((s: String, b: Boolean) ⇒ (s, b))) { data: (String, Boolean) ⇒
        val (name, active) = data

        val updateResult = persistenceModule.updateWithGeneratedKeys[(String, Boolean), Long](
          sql = insertSql,
          fields = List("id"),
          values = data
        ).transact(trx).attemptRun

        updateResult shouldBe a[\/-[PersistenceItem]]
        updateResult match {
          case \/-(id: Long) ⇒
            fetchItemById(id).transact(trx).attemptRun shouldBe \/-[PersistenceItem](PersistenceItem(id, name, active))
        }
      }
    }
  }

  "updateMany" should {
    "return the number of affected rows after inserting a batch of items in the table" in {

      forAll(dataStringBoolean.arbitrary) { (data: List[(String, Boolean)]) ⇒

        persistenceModule.updateMany[List, (String, Boolean)](
          sql = insertSql,
          values = data
        ).transact(trx).attemptRun shouldBe \/-[Int](data.size)
      }
    }

    "return the number of affected rows equals to 0 after updating a batch of items " +
      "in the table if the table is empty" in {

        forAll(dataStringBooleanLong.arbitrary) { (data: List[(String, Boolean, Long)]) ⇒
          persistenceModule.updateMany[List, (String, Boolean, Long)](
            sql = updateByIdSql,
            values = data
          ).transact(trx).attemptRun shouldBe \/-[Int](0)
        }
      }
    "return the number of affected rows after updating a batch of items in the table " +
      "if the items exist" in {

        forAll(dataStringBoolean.arbitrary) { (data: List[(String, Boolean)]) ⇒
          val fetchData = {
            for {
              _ ← insertItems(data)
              result ← fetchAll
            } yield result
          }.transact(trx).run map {
            case (name, active, id) ⇒ (name, !active, id)
          }

          persistenceModule.updateMany[List, (String, Boolean, Long)](
            sql = updateByIdSql,
            values = fetchData
          ).transact(trx).attemptRun shouldBe \/-[Int](fetchData.size)
        }
      }
  }
}
