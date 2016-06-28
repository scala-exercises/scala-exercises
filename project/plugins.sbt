// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

resolvers += Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(Resolver.ivyStylePatterns)


// Sbt plugins
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.4.8")

addSbtPlugin("org.scala-js" % "sbt-scalajs" % "0.6.9")

addSbtPlugin("com.vmunier" % "sbt-play-scalajs" % "0.2.6")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("default" % "sbt-sass" % "0.1.9")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

// Build common plugin
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.5.1")
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// Scripted
libraryDependencies <+= sbtVersion(v => "org.scala-sbt" % "scripted-plugin" % v)
