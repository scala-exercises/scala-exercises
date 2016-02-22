[![Build Status](https://api.travis-ci.org/47deg/scala-exercises.svg?branch=version-2)](https://travis-ci.org/47deg/scala-exercises?branch=version-2) [![Stories in Ready](https://badge.waffle.io/47deg/scala-exercises.png?label=ready&title=Ready)](https://waffle.io/47deg/scala-exercises)

[![Join the chat at https://gitter.im/47deg/scala-exercises](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/47deg/scala-exercises?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

# Scala Exercises

This is second iteration of Scala Exercises. Version 1 is available by going to the
[master](https://github.com/47deg/scala-exercises/tree/master) branch.

Version 2 is under active development and is rapidly approaching a public release.

## Getting Started Locally

The project has two main directories, `core` and `site`, each with a SBT project.

The `core` directory contains an exercise runtime and exercise compiler. These allow
exercises to be defined using regular Scala which is compiled into an exercise
library.

The `site` directory contains the Play website as well as some pre-packaged
exercise content. These items depend on components in `core`.

At the moment, `site` and `core` are coupled tightly. Once this project
is a bit more stable the exercise compiler plugin will be published and it will
be easy to create new exercises for existing Scala libraries.

If you'd like to run the server, you will need to set up PostgreSQL locally.

    // TODO: bootstrap DB steps here

Once this is done, you can navigate to the `site` directory and launch
SBT. From there, `run` should launch the Play app.

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
