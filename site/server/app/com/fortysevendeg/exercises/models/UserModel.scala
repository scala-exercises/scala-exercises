package com.fortysevendeg.exercises.models

import doobie.imports._
import scala.concurrent.Future
import shared.User
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import slick.driver._
import slick.jdbc.JdbcBackend.Database
import scalaz.concurrent.Task

trait OldUserStore {

  def all: Future[Seq[User]]

  def getByLogin(login: String): Future[Seq[User]]

  def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Future[User]

  def update(user: User): Future[Boolean]

  def delete(ids: Long*): Future[Boolean]
}

trait UserStore {
  def all: List[User]
}

class UserSlickStore(db: Database) extends OldUserStore {

  lazy val profile = current.configuration.getString("db.default.driver").collect {
    case "org.h2.Driver"         ⇒ H2Driver
    case "org.postgresql.Driver" ⇒ PostgresDriver
    case "com.mysql.jdbc.Driver" ⇒ MySQLDriver
    case other                   ⇒ throw new Exception(s"Unable to discern Slick driver for ${other}")
  }.get

  import play.api.db.DB
  import profile.api._

  class Users(tag: Tag) extends Table[User](tag, "users") {
    def id = column[Option[Long]]("id", O.PrimaryKey, O.AutoInc)
    def login = column[String]("login")
    def name = column[String]("name")
    def github_id = column[String]("github_id")
    def picture_url = column[String]("picture_url")
    def github_url = column[String]("github_url")
    def email = column[String]("email")
    def * = (id, login, name, github_id, picture_url, github_url, email) <> (User.tupled, User.unapply)
  }

  val users = TableQuery[Users]

  override def all: Future[Seq[User]] = {
    db.run(users.sortBy(_.id.desc).result)
  }

  override def getByLogin(login: String): Future[Seq[User]] = db.run(users.filter(_.login === login).result)

  override def create(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ): Future[User] = {

    db.run {
      (users returning users.map(_.id) into ((user, id) ⇒ user.copy(id = id))) += User(
        id = None,
        login = login,
        name = name,
        github_id = github_id,
        picture_url = picture_url,
        github_url = github_url,
        email = email
      )
    }
  }

  override def update(user: User): Future[Boolean] = {
    db.run {
      val q = for {
        u ← users if u.id === user.id
      } yield (u.login)
      q.update(user.login)
    }.map(_ == 1)
  }

  override def delete(ids: Long*): Future[Boolean] = {
    Future.sequence {
      for {
        id ← ids
      } yield { db.run(users.filter(_.id === id).delete).map(_ == 1) }
    }.map(!_.exists(i ⇒ !i))
  }

}
import com.fortysevendeg.exercises.services.persistence.Persistence

class UserDoobieStore(implicit transactor: Transactor[Task]) extends UserStore {
  def all: List[User] = {
    val query = Persistence.fetchList[User](
      "select id, login, name, github_id, picture_url, github_url, email from users"
    )
    query.transact(transactor).run
  }
}

object UserStore {
  implicit def instance(implicit transactor: Transactor[Task]): UserStore = new UserDoobieStore
}
