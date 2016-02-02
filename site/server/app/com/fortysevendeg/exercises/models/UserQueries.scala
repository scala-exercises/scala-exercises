package com.fortysevendeg.exercises.models.queries

import shared.User
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

  def byId(id: Int) =
    sql"""
SELECT id, login, name, githubId, pictureUrl, githubUrl, email
FROM User
WHERE id = $id
""".query[User]

  def insert(
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ) =
    sql"""
INSERT INTO User (login, name, githubId, pictureUrl, githubUrl, email)
VALUES ($login, $name, $githubId, $pictureUrl, $githubUrl, $email)
""".update

  def deleteById(id: Int) =
    sql"""
DELETE FROM User
      WHERE id = $id
    """.update

  def delete(id: Int): ConnectionIO[Boolean] = for {
    beforeDeletion ← byId(id).option
    _ ← deleteById(id).run
  } yield beforeDeletion.isDefined

  def deleteAll =
    sql"""
DELETE FROM User
    """.update

  def update(
    id:         Int,
    login:      String,
    name:       String,
    githubId:   String,
    pictureUrl: String,
    githubUrl:  String,
    email:      String
  ) =
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
