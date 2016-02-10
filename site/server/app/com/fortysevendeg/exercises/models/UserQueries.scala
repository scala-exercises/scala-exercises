package com.fortysevendeg.exercises.models.queries

import doobie.imports._

import shared.User
import com.fortysevendeg.exercises.models.UserCreation

object Queries {
  val all = sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM users
""".query[User]

  def byLogin(login: String) =
    sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM users
WHERE login = $login
""".query[User]

  def byId(id: Long) =
    sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM users
WHERE id = $id
""".query[User]

  def insert(user: UserCreation.Request) = {
    val UserCreation.Request(login, name, githubId, pictureUrl, githubUrl, email) = user
    sql"""
INSERT INTO users (login, name, githubId, pictureUrl, githubUrl, email)
VALUES ($login, $name, $githubId, $pictureUrl, $githubUrl, $email)
""".update
  }

  def deleteById(id: Long) =
    sql"""
DELETE FROM users
      WHERE id = $id
    """.update

  def delete(id: Long): ConnectionIO[Boolean] = for {
    beforeDeletion ← byId(id).option
    _ ← deleteById(id).run
  } yield beforeDeletion.isDefined

  def deleteAll =
    sql"""
DELETE FROM users
    """.update

  def update(user: User) = {
    val User(id, _, name, githubId, pictureUrl, githubUrl, email) = user
    sql"""
UPDATE users
SET    name = $name,
       githubId = $githubId,
       pictureUrl = $pictureUrl,
       githubUrl = $githubUrl,
       email = $email
WHERE id = $id;
""".update
  }
}
