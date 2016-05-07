[![Stories in Ready](https://badge.waffle.io/47deg/scala-exercises.png?label=ready&title=Ready)](https://waffle.io/47deg/scala-exercises)
[![Build status](https://img.shields.io/travis/scala-exercises/scala-exercises.svg)](https://travis-ci.org/scala-exercises/scala-exercises)
[![Join the conversation on Gitter](https://img.shields.io/gitter/room/47deg/scala-exercises.svg)](https://gitter.im/47deg/scala-exercises)

#Scala Exercises

------------------------

## How it works

"Scala Exercises" brings exercises for the Stdlib, Cats, Shapeless and many other great libraries for Scala to your browser. Offering hundreds of solvable exercises organized into several categories covering the basics of the Scala language and it's most important libraries.

- LEARN: Each category includes an explanation of the basics. Learn the concepts through simple code samples.

- SOLVE: Each exercise is a unit test that must pass successfully, complete the exercise by filling in the blanks. Receive instant feedback as your answers are validated in real-time.

- SHARE: The system will consider the category complete when all its exercises are successfully done. Don't forget to share your progress on social networks before moving on to the next category!

- EDIT: After completing a category, you'll be able to go back and edit it. Add new exercises or improve existing ones by sending a pull-request.


## Getting Started

### Prerequisites

- Install JDK 8, either the [Oracle version](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK](http://openjdk.java.net/projects/jdk8/)
- Install [Scala](http://scala-lang.org/download/)
- Install [SBT](http://www.scala-sbt.org/download.html)
- Install [PostgreSQL 9.4](http://www.postgresql.org/download/)
- Install the [Sass Ruby gem](http://sass-lang.com/install) and make sure the `sass` program can be run

### Installing the app locally

First of all, either clone the repository via git

```sh
$ git clone https://github.com/scala-exercises/scala-exercises
```

or download it

```sh
$ wget https://github.com/scala-exercises/scala-exercises/archive/master.zip
```

You'll need a working PostgreSQL 9.4 database and user for running the app. Once the database is running,
create a user called `scalaexercises_dev_user`

```sh
$ createuser -P -e scalaexercises_dev_user "a_password"
```

Create a db called `scalaexercises_dev` and grant all privileges on it to `scalaexercises_dev_user`

```sh
$ createdb scalaexercises_dev
$ psql -c "GRANT ALL PRIVILEGES ON DATABASE scalaexercises_dev TO scalaexercises_dev_user;"
```

Edit the `site/server/conf/application.dev.conf` configuration file with your database information.

### Running the app

Go into the `site` directory, run `sbt run`

```sh
$ cd site/
$ sbt run
```

After compilation the application will be running, listening in the 9000 port. Point your browser
to `localhost:9000` and start having fun!

### Troubleshooting

If you use *ensime* and you have configured the `sbt-ensime` plugin in your sbt user
global settings, likely you might have this issue running the application locally:

```java.lang.NoClassDefFoundError: scalariform/formatter/preferences/SpacesAroundMultiImports$```

In that case, you could solve this issue setting up your `/.sbt/0.13/plugins/plugins.sbt` file
as follow:

```scala
addSbtPlugin("org.ensime" % "ensime-sbt" % "0.4.0")

dependencyOverrides in ThisBuild += "org.scalariform" %% "scalariform" % "0.1.8"
```

## Project structure

The project has two main directories, `core` and `site`, each with a SBT project.

The `core` directory contains an exercise runtime and exercise compiler. These allow
exercises to be defined using regular Scala which is compiled into an exercise
library.

The `site` directory contains the Play website as well as some pre-packaged
exercise content. These items depend on components in `core`.

At the moment, `site` and `core` are coupled tightly. Once this project
is a bit more stable the exercise compiler plugin will be published and it will
be easy to create new exercises for existing Scala libraries.

## Contributing

Contributions welcome! At this time, we don't have an official contribution
guide, so please join our [Gitter channel](https://gitter.im/47deg/scala-exercises)
to get involved.

##License

Copyright (C) 2015-2016 47 Degrees, LLC.
Reactive, scalable software solutions.
http://47deg.com
hello@47deg.com

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
