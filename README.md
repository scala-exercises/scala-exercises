[![Stories in Ready](https://badge.waffle.io/47deg/scala-exercises.png?label=ready&title=Ready)](https://waffle.io/47deg/scala-exercises)
[![Build status](https://img.shields.io/travis/47deg/scala-exercises.svg)](https://travis-ci.org/47deg/scala-exercises)
[![Join the conversation on Gitter](https://img.shields.io/gitter/room/47deg/scala-exercises.svg)](https://gitter.im/47deg/scala-exercises)

#Scala Exercises

------------------------

## How it works

"Scala Exercises" brings exercises for the Stdlib, Cats, Shapeless and many other great libraries for Scala to your browser. Offering hundreds of solvable exercises organized into several categories covering the basics of the Scala language and it's most important libraries.

- LEARN: Each category includes an explanation of the basics. Learn the concepts through simple code samples.

- SOLVE: Each exercise is a unit test that must pass successfully, complete the exercise by filling in the blanks. Receive instant feedback as your answers are validated in real-time.

- SHARE: The system will consider the category complete when all its exercises are successfully done. Don't forget to share your progress on social networks before moving on to the next category!

- EDIT: After completing a category, you'll be able to go back and edit it. Add new exercises or improve existing ones by sending a pull-request.


## Getting Started Locally

If you wish to contribute to this project you'd need to run it locally.

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
