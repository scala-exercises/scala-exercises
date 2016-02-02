package com.fortysevendeg.exercises.models.queries

import shared.User
import com.fortysevendeg.exercises.models.NewUser
import doobie.imports._

object Queries {
  val all = sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM User
""".query[User]

  def byLogin(login: String) =
    sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM User
WHERE login = $login
""".query[User]

  def byId(id: Long) =
    sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM User
WHERE id = $id
""".query[User]

  def insert(user: NewUser) = {
    val NewUser(login, name, githubId, pictureUrl, githubUrl, email) = user
    sql"""
INSERT INTO User (login, name, githubId, pictureUrl, githubUrl, email)
VALUES ($login, $name, $githubId, $pictureUrl, $githubUrl, $email)
""".update
  }

  def deleteById(id: Long) =
    sql"""
DELETE FROM User
      WHERE id = $id
    """.update

  def delete(id: Long): ConnectionIO[Boolean] = for {
    beforeDeletion ← byId(id).option
    _ ← deleteById(id).run
  } yield beforeDeletion.isDefined

  def deleteAll =
    sql"""
DELETE FROM User
    """.update

  def update(user: User) = {
    val User(id, _, name, githubId, pictureUrl, githubUrl, email) = user
    sql"""
UPDATE User
SET    name = $name,
       githubId = $githubId,
       pictureUrl = $pictureUrl,
       githubUrl = $githubUrl,
       email = $email
WHERE id = $id;
""".update
  }
}
