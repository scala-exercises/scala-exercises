package org.scalaexercises.types.evaluator

final case class Dependency(
  groupId:    String,
  artifactId: String,
  version:    String
)
