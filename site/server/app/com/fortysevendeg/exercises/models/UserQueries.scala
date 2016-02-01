package com.fortysevendeg.exercises.models.queries

import shared.User
import doobie.imports._

object Queries {
  val all = sql"""
SELECT id, login, name, github_id, picture_url, github_url, email
FROM users
""".query[User]

  def byLogin(login: String) =
    sql"""
SELECT id, login, name, github_id, picture_url, github_url, email
FROM users
WHERE login = $login
""".query[User]

  def byId(id: Int) =
    sql"""
SELECT id, login, name, github_id, picture_url, github_url, email
FROM users
WHERE id = $id
""".query[User]

  def insert(
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ) =
    sql"""
INSERT INTO users (login, name, github_id, picture_url, github_url, email)
VALUES ($login, $name, $github_id, $picture_url, $github_url, $email)
""".update

  def deleteById(id: Int) =
    sql"""
DELETE FROM users
      WHERE id = $id
    """.update

  def delete(id: Int): ConnectionIO[Boolean] = for {
    beforeDeletion ← byId(id).option
    _ ← deleteById(id).run
  } yield beforeDeletion.isDefined

  def deleteAll =
    sql"""
DELETE FROM users
    """.update

  def update(
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ) =
    sql"""
UPDATE users
SET    name = $name,
       github_id = $github_id,
       picture_url = $picture_url,
       github_url = $github_url,
       email = $email
WHERE id = $id;
""".update

}
