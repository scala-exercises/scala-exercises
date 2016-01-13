// Build common plugin
// This override is a nasty hack to circumvent some dependency bug in SBT
dependencyOverrides += "org.scalariform" %% "scalariform" % "0.1.8"
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")
unmanagedSourceDirectories in Compile += baseDirectory.value.getParentFile.getParentFile / "common"
