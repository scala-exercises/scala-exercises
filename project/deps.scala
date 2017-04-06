import sbt.Keys._
import sbt._

trait deps {

  val v = Map(
    // General Versions
    'project -> "0.3.0-SNAPSHOT",
    'scala -> "2.11.8"
  ) ++ Map(
    // JVM Versions
    'bootstrap -> "3.2.0",
    'cats -> "0.7.2",
    'classutil -> "1.0.11",
    'commonsio -> "2.5",
    'config -> "1.3.0",
    'doobie -> "0.3.0",
    'evaluator -> "0.1.1-SNAPSHOT",
    'github4s -> "0.8.0-SNAPSHOT",
    'highlightjs -> "9.2.0",
    'kindprojector -> "0.9.0",
    'knockoff -> "0.8.3",
    'log4s -> "1.3.0",
    'monix -> "2.0.3",
    'newrelic -> "3.29.0",
    'paradise -> "2.1.0",
    'postgres -> "9.3-1102-jdbc41",
    'scalajsscripts -> "0.2.1",
    'scalariform -> "0.1.8",
    'scalaz -> "7.2.4",
    'scalazspecs2 -> "0.4.0",
    'scalacheck -> "1.12.5",
    'scalacheckshapeless -> "0.3.1",
    'scalaTest -> "2.2.6",
    'simulacrum -> "0.8.0",
    'slf4j -> "1.6.4",
    'upickle -> "0.2.8",
    'utileval -> "6.34.0",
    'webjars -> "2.3.0"
  ) ++ Map(
    // JS Versions
    'scalajsdom -> "0.8.1",
    'scalatags -> "0.5.2",
    'scalajsjquery -> "0.8.1",
    'utest -> "0.2.8",
    'scalacscoverage -> "1.1.0-JS"
  )

  implicit class Exclude(module: ModuleID) {

    def x(org: String, artifact: String): ModuleID = module.exclude(org, artifact)

    def xscalaz: ModuleID = x("org.scalaz", "scalaz-concurrent")

    def xscalajs: ModuleID = x("org.scala-js", "scalajs-library") x("org.scala-js", "scalajs-dom")
  }

}
