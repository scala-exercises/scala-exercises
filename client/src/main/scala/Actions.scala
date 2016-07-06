/*
 * scala-exercises-client
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.client
package actions

import model.Exercises._

sealed trait Action
case object Start extends Action
case class SetState(s: State) extends Action
case class UpdateExercise(method: String, args: Seq[String]) extends Action
case class CompileExercise(method: String) extends Action
case class CompilationOk(method: String) extends Action
case class CompilationFail(method: String, msg: String) extends Action
