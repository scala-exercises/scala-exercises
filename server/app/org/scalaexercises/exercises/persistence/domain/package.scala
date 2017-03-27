/*
 * scala-exercises - server
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package org.scalaexercises.exercises.persistence

import shapeless.{HList, LabelledGeneric}
import shapeless.ops.record.Keys
import shapeless.ops.hlist.ToTraversable

package object domain {

  /** Get the field names of a case class */
  private[domain] def fieldNames[CC]: FieldNamesPartlyApplied[CC] =
    new FieldNamesPartlyApplied[CC]

  private[domain] class FieldNamesPartlyApplied[CC] {
    // format: OFF
    def apply[LG <: HList, K <: HList]()(
      implicit
      lgen: LabelledGeneric.Aux[CC, LG],
      keys: Keys.Aux[LG, K],
      toList: ToTraversable[K, List]
    ): List[String] = toList(keys()).map { case Symbol(s) ⇒ s.toLowerCase }
    // format: ON
  }

}
