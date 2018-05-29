[![Stories in Ready](https://badge.waffle.io/47deg/scala-exercises.png?label=ready&title=Ready)](https://waffle.io/47deg/scala-exercises)
[![Build Status](https://travis-ci.org/scala-exercises/scala-exercises.svg?branch=master)](https://travis-ci.org/scala-exercises/scala-exercises)
[![Join the conversation on Gitter](https://img.shields.io/gitter/room/47deg/scala-exercises.svg)](https://gitter.im/scala-exercises/scala-exercises)
[![codecov.io](http://codecov.io/github/scala-exercises/scala-exercises/coverage.svg?branch=master)](http://codecov.io/github/scala-exercises/scala-exercises?branch=master)

# Scala Exercises

------------------------

## How it works

"Scala Exercises" brings exercises for Stdlib, Cats, Shapeless and many other great libraries for Scala to your browser. It offers hundreds of solvable exercises organized into several categories covering the basics of the Scala language and its most important libraries.

- LEARN: Each category includes an explanation of the basics. Learn the concepts through simple code samples.

- SOLVE: Each exercise is an unit test that must pass successfully, complete the exercise by filling in the blanks. Receive instant feedback as your answers are validated in real-time.

- SHARE: The system will consider the category complete when all of its exercises are successfully done. Don't forget to share your progress on social networks before moving on to the next category!

- EDIT: After completing a category, you'll be able to go back and edit it. Add new exercises or improve existing ones by sending a pull-request.


## Getting Started

### Online

Scala Exercises is available at [scala-exercises.org](https://scala-exercises.org).

### Local development

#### Prerequisites

- Install JDK 8, either the [Oracle version](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or [OpenJDK](http://openjdk.java.net/projects/jdk8/)
- Install [Scala](http://scala-lang.org/download/)
- Install [SBT](http://www.scala-sbt.org/download.html)
- Install [PostgreSQL 9.4](http://www.postgresql.org/download/)
- Install the [Sass Ruby gem](http://sass-lang.com/install) and make sure the `sass` program can be run
- Install [jsdom](https://github.com/tmpvar/jsdom) with `npm install jsdom`

#### Installing the app locally

##### Get the repository
First of all, either clone the repository via git

```sh
$ git clone https://github.com/scala-exercises/scala-exercises
```

or download it

```sh
$ wget https://github.com/scala-exercises/scala-exercises/archive/master.zip
```
##### Configure the database

You'll need a working [PostgreSQL 9.4](http://www.postgresql.org/download/) database and user for running the app. Once the database is running,

- Create a user called `scalaexercises_dev_user`

```sh
$ sudo -u postgres psql -c "CREATE USER scalaexercises_dev_user WITH PASSWORD 'a_password';"
```

- Create a db called `scalaexercises_dev` and grant all privileges on it to `scalaexercises_dev_user`

```sh
$ sudo -u postgres createdb scalaexercises_dev
$ sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE scalaexercises_dev TO scalaexercises_dev_user;"
```

Alternatively, you can also use Docker to run the database. The following command creates a database container and exposes it:

```sh
$ docker run --name scala-exercises-db -e POSTGRES_DB=scalaexercises_dev -e POSTGRES_PASSWORD=scalaexercises_pass -e POSTGRES_USER=scalaexercises_dev_user -p 5432:5432 -d postgres:9.4
```

##### Configure the application

Edit the `server/conf/application.dev.conf` configuration file with your database information.

#### Running the app

Go into the project's root directory, run `sbt server/run` with `-mem` option to increase the memory

```sh
$ sbt -mem 1500 server/run
```

After compilation the application will be running, listening in the 9000 port. Point your browser
to `localhost:9000` and start having fun!

#### Running the tests

To run the tests (for the `server` project), you need to add a test database and a test user.

- Create a user called `scalaexercises_user`

```sh
$ sudo -u postgres psql -c "CREATE USER scalaexercises_user WITH PASSWORD 'scalaexercises_pass';"
```

- Create a db called `scalaexercises_test` and grant all privileges on it to `scalaexercises_user`

```sh
$ sudo -u postgres createdb scalaexercises_test
$ sudo -u postgres psql -c "GRANT ALL PRIVILEGES ON DATABASE scalaexercises_test TO scalaexercises_user;"
```

#### Adding more exercises

Currently scala-exercises includes exercises for the Scala Standard Library, Cats and Shapeless. However, more exercises are available like for Doobie, Functional Programming in Scala and ScalaCheck. See the [scala-exercises on github](https://github.com/scala-exercises) or you can include exercises from other parties or create your own (see [Contributing](#contributing) section).

To add additional exercises to your locally running server:
* clone the exercises repository to a local folder
* 'cd' into the local repository folder.
* run ```sbt compile publishLocal``` to create a jar in your local ivy repository.  
!Note: The _compile_ task is **mandatory** here otherwise the exercises will not show up in the application.
* add a dependency to the exersises jar in the `server` project in the `build.sbt` file (~L118).

Now run `sbt server/run` and the application index will also display the added exercises.

## Troubleshooting

#### Additional exercises do not show up in the application

See the [Adding more exercises](#adding-more-exercises) section. Note that currently the `compile` step is required before `publishLocal` for the application to be able to pickup the exercises.

#### Ensime

If you use *ensime* and you have configured the `sbt-ensime` plugin in your sbt user
global settings, likely you might have this issue running the application locally:

```java.lang.NoClassDefFoundError: scalariform/formatter/preferences/SpacesAroundMultiImports$```

In that case, you could solve this issue setting up your `/.sbt/0.13/plugins/plugins.sbt` file
as follow:

```scala
addSbtPlugin("org.ensime" % "ensime-sbt" % "0.5.1")

dependencyOverrides in ThisBuild += "org.scalariform" %% "scalariform" % "0.1.8"
```

In order to avoid the error related to `Github API rate limit exceeded` during compilation of exercises, we recommend locally setting an environment variable called `GITHUB_TOKEN` with a personal token which you can create [here](https://github.com/settings/tokens/new).

While creating the PostgreSQL database, you may run into problems following the previous instructions if developing on a MacOS X environment. In that case we recommend using the following alternative ones:

- Create a user called `scalaexercises_dev_user`. Note that if you installed PostgreSQL using Homebrew, your superuser may be different than `postgres`:

```sh
$ psql -U your_postgres_user -c "CREATE USER scalaexercises_dev_user WITH PASSWORD 'a_password';"
```

- Create a db called `scalaexercises_dev` and grant all privileges on it to `scalaexercises_dev_user`:

```sh
$ createdb scalaexercises_dev
$ psql -U your_postgres_user -c "GRANT ALL PRIVILEGES ON DATABASE scalaexercises_dev TO scalaexercises_dev_user;"
```

## Project structure

The project is split between a few directories, namely:
- `server`, which contains the server code written using Play,
- `client`, which contains ScalaJS code for a frontend part of the application,
- `shared`, where code shared between the server and the client exists,
- `definitions`, containing definitions used by other parts of the application and libraries containing exercises,
- `sbt-exercise` is a sbt plugin which locates exercise libraries and processes their source code,
- `compiler` for compiling exercises,
- `runtime` for runtime evaluation of exercises.

The `compiler` and `runtime` directories allow exercises to be defined using
regular Scala which is compiled into an exercise library.

The `site`, `client` and `shared` directories contain the website. These items depend on components in `compiler` and `runtime`.

At the moment, those subprojects are coupled tightly. Once this project
becomes a bit more stable, the exercise compiler plugin will be published and it will
be easy to create new exercises for existing Scala libraries.

## Contributing

Contributions welcome! Please join our [Gitter channel](https://gitter.im/scala-exercises/scala-exercises)
to get involved.

Feel free to open an issue if you notice a bug, have an idea for a
feature, or have a question about the code. Pull requests are also
gladly accepted.

In the same fashion, if you're interested in providing your own content
for your library (or a third-party's), you can find more information
on how to do it in the [Developer Guide](DEV_GUIDE.md).

People are expected to follow the
[Typelevel Code of Conduct](http://typelevel.org/conduct.html) when
discussing Scala Exercises on the Github page, Gitter channel, or other
venues.

We hope that our community will be respectful, helpful, and kind. If
you find yourself embroiled in a situation that becomes heated, or
that fails to live up to our expectations, you should disengage and
contact one of the project maintainers in private.

## License

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
