/*
 * scala-exercises - definitions
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.definitions

trait BuildInfo {
  def name: String
  def version: String
  def scalaVersion: String
  def organization: String
  def resolvers: scala.collection.Seq[String]
  def libraryDependencies: scala.collection.Seq[String]
}