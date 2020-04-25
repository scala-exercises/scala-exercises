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

package org.scalaexercises.evaluator.util

import cats.effect.Sync
import io.circe.generic.semiauto
import io.circe.generic.semiauto.deriveDecoder
import io.circe.syntax.EncoderOps
import io.circe.{Decoder, Encoder, Json}
import org.http4s.circe._
import org.http4s.{EntityDecoder, EntityEncoder}
import org.scalaexercises.evaluator.types._

object Codecs {

  implicit val decodeRangePosition: Decoder[RangePosition] = deriveDecoder[RangePosition]

  implicit val decodeCompilationInfo: Decoder[CompilationInfo] = deriveDecoder[CompilationInfo]

  implicit val decodeEvalResponse: Decoder[EvalResponse] = deriveDecoder[EvalResponse]

  implicit val encodeEvalRequest: Encoder[EvalRequest] = Encoder.instance(req =>
    Json.obj(
      ("resolvers", Json.arr(req.resolvers.map(Json.fromString): _*)),
      ("dependencies", Json.arr(req.dependencies.map(_.asJson): _*)),
      ("code", Json.fromString(req.code))
    )
  )

  implicit val encodeDependency: Encoder[EvaluatorDependency] = Encoder.instance(dep =>
    Json.obj(
      ("groupId", Json.fromString(dep.groupId)),
      ("artifactId", Json.fromString(dep.artifactId)),
      ("version", Json.fromString(dep.version)),
      ("exclusions", dep.exclusions.asJson)
    )
  )

  implicit val encodeExclusion: Encoder[Exclusion] = semiauto.deriveEncoder[Exclusion]

  implicit def decoder[F[_]: Sync, A: Decoder]: EntityDecoder[F, A] = jsonOf[F, A]

  implicit def encoder[F[_]: Sync, A: Encoder]: EntityEncoder[F, A] = jsonEncoderOf[F, A]

}
