package com.fortysevendeg.exercises.models.queries

import shared.User
import doobie.imports._

object Queries {
  val TABLE = "users"
  val ALL_FIELDS = "id, login, name, github_id, picture_url, github_url, email"

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
    id:          Int,
    login:       String,
    name:        String,
    github_id:   String,
    picture_url: String,
    github_url:  String,
    email:       String
  ) =
    sql"""
INSERT INTO users
VALUES ($id, $login, $name, $github_id, $picture_url, $github_url, $email)
""".update

  def delete(id: Int) =
    sql"""
DELETE FROM users
      WHERE id = $id
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
