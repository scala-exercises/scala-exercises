// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
  "Typesafe Maven Releases" at "http://repo.typesafe.com/typesafe/maven-releases/",
  Resolver.url("GitHub repository", url("http://shaggyyeti.github.io/releases"))(
    Resolver.ivyStylePatterns),
  Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(
    Resolver.ivyStylePatterns)
)

// Sbt plugins
addSbtPlugin("com.47deg"         % "sbt-org-policies" % "0.5.13")
addSbtPlugin("com.typesafe.play" % "sbt-plugin"       % "2.4.8")
addSbtPlugin("com.vmunier"       % "sbt-web-scalajs"  % "1.0.3")
addSbtPlugin("com.typesafe.sbt"  % "sbt-gzip"         % "1.0.0")
addSbtPlugin("default"           % "sbt-sass"         % "0.1.9")
addSbtPlugin("org.wartremover"   % "sbt-wartremover"  % "2.0.2")

// Scripted
libraryDependencies += "org.scala-sbt" % "scripted-plugin" % sbtVersion.value
