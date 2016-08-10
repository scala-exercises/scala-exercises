package org.scalaexercises.definitions

trait BuildInfo {
  def resolvers: scala.collection.Seq[String]
  def libraryDependencies: scala.collection.Seq[String]
}