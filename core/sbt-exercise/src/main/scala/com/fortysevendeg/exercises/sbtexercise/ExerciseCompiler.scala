
package com.fortysevendeg.exercises
package sbtexercise

import sbt._
import sbt.classpath.{ ClasspathUtilities ⇒ CPU }
import sbt.Keys._

import scala.util.{ Try, Success, Failure }

import java.io.File

object ExerciseCompiler {

  private val DOC_GLOBAL = "com.fortysevendeg.exercises.compiler.DocExtractionGlobal$Java"

  def compile(
    sourceDirectories: Seq[File],
    targetDirectory:   File,
    fork:              Boolean,
    log:               Logger
  ) {

    val exercisePaths = sourceDirectories.map(_.getPath)
    log.info(s"Launching exercise compiler (fork = $fork)")

    def logExitCode(exitCode: Int) {
      log.info(s"~> exit code $exitCode")
    }

    fork match {
      case true ⇒
        log.error("forking currently disabled, please use `fork in compileExercises in Compile := false`")
        ???

      case false ⇒

        type GLOBAL = {
          def findAll(code: String): java.util.Map[String, String]
        }

        val loader = CPU.toLoader(
          Meta.compilerClasspath,
          null,
          CPU.createClasspathResources(
            appPaths = Meta.compilerClasspath,
            bootPaths = Nil
          )
        )

        val res = for {
          globalClass ← Try(loader.loadClass(DOC_GLOBAL))
          global ← Try(globalClass.newInstance.asInstanceOf[GLOBAL])
        } yield {
          val code = """
            /** This is a comment that gets ignored */
            package myPackage {
              /** This is Foo */
              class Foo { val value = 1 }
              /** This is Bar */
              object Bar {
                /** This is Bar.bar */
                def bar() {}
                object fizz {
                  /** This is SubBar */
                  object SubBar {
                    /** This is SubBar.subbar */
                    def subbar() {}
                  }
                }
              }
            }"""
          val res = global.findAll(code)
          println("RES " + res)
        }

        res match {
          case Failure(e) ⇒
            log.error(s"~ error ${e.getMessage}")
            e.printStackTrace()
          case _ ⇒
        }
    }
  }

}
