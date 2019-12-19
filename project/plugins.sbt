// Comment to get more information during initialization
logLevel := Level.Warn

// Resolvers
resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.typesafeRepo("http://repo.typesafe.com/typesafe/maven-releases/"),
  Resolver.typesafeIvyRepo("http://repo.typesafe.com/typesafe/releases/")
)

// Sbt plugins
addSbtPlugin("org.irundaia.sbt"   % "sbt-sassify"               % "1.4.13")
addSbtPlugin("org.scala-js"       % "sbt-scalajs"               % "0.6.31")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"             % "0.9.0")
addSbtPlugin("com.47deg"          % "sbt-org-policies"          % "0.12.0-M3")
addSbtPlugin("com.typesafe.play"  % "sbt-plugin"                % "2.8.0")
addSbtPlugin("org.portable-scala" % "sbt-scalajs-crossproject"  % "0.6.1")
addSbtPlugin("com.vmunier"        % "sbt-web-scalajs"           % "1.0.9-0.6")
addSbtPlugin("com.typesafe.sbt"   % "sbt-gzip"                  % "1.0.2")
addSbtPlugin("com.typesafe.sbt"   % "sbt-native-packager"       % "1.5.2")
addSbtPlugin("com.github.cb372"   % "sbt-explicit-dependencies" % "0.2.11")