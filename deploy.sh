#!/bin/bash

sbt dist

bees app:deploy -a play-scalajs -t play2 scalajvm/target/universal/play-example-0.1.0-SNAPSHOT.zip -R java_version=1.7
