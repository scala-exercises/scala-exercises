/*
 * Copyright 2014-2020 47 Degrees <https://47deg.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.scalaexercises.evaluator

import cats.effect.{ConcurrentEffect, Resource}
import org.http4s.client.Client
import org.http4s.client.blaze.BlazeClientBuilder
import org.scalaexercises.evaluator.service.{HttpClientHandler, HttpClientService}

import scala.concurrent.ExecutionContext

object EvaluatorClient {

  private def clientResource[F[_]: ConcurrentEffect]: Resource[F, Client[F]] =
    BlazeClientBuilder[F](ExecutionContext.global).resource

  def apply[F[_]: ConcurrentEffect](url: String, authKey: String): HttpClientService[F] =
    HttpClientHandler[F](url, authKey, clientResource[F])

}
