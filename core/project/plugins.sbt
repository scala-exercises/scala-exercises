// Build common plugin
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile.getParentFile / "common"
