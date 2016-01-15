// Build common plugin
addSbtPlugin("org.brianmckenna" % "sbt-wartremover" % "0.14")
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile.getParentFile / "common"
