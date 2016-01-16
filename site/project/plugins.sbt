// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)


// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.0")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.5")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

// Build common plugin
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile.getParentFile / "common"

// Exercise compiler plugin
lazy val build = (project in file("."))
  .dependsOn(ProjectRef(file("../../core"), "sbt-exercise"))

// **Note**
// This is only here so that the site project can be tricked into finding
// the compiler project when building the sbt-exercise plugin.
// For some reason SBT barfs when loading site with:
//   'compiler/compile:fullClasspath is undefined'
// ...unless of course we spoof the project with this BS proxy...
lazy val compiler = (project in file(".compiler-proxy"))
  .settings(scalaVersion := "2.11.7") // *@@*!! qq
  .dependsOn(ProjectRef(file("../../core"), "compiler"))
