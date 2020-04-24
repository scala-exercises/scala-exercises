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

package org.scalaexercises.evaluator.service

import cats.effect.{Resource, Sync}
import cats.implicits._
import org.http4s.client.Client
import org.http4s.{Header, Method, Request, Uri}
import org.scalaexercises.evaluator.types._
import org.scalaexercises.evaluator.util.Codecs._

object HttpClientHandler {

  private def headerToken(value: String) = Header("x-scala-eval-api-token", value)

  private val headerContentType = Header("content-type", "application/json")

  def apply[F[_]](uri: String, authString: String, resource: Resource[F, Client[F]])(
      implicit F: Sync[F]
  ): HttpClientService[F] =
    new HttpClientService[F] {
      override def evaluates(evalRequest: EvalRequest): F[EvalResponse] =
        for {
          uri <- F.fromEither(Uri.fromString(uri))
          request = Request[F](Method.POST, uri)
            .withEntity(evalRequest)
            .withHeaders(headerToken(authString), headerContentType)
          result <- resource.use(_.expect[EvalResponse](request))
        } yield result
    }

}
