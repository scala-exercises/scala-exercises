package exercises

import stdlib._
import shapeless.HNil

import org.scalatest.Spec
import org.scalatest.prop.Checkers

// FIXME: get rid of this if possible
import org.scalacheck.Shapeless._

class TraitsSpec extends Spec with Checkers {
  def `are similar to interfaces` = {
    check(
      Test.success(
        Traits.similarToInterfacesTraits _,
        "An unfortunate moose stampede occurred" :: HNil
      )
    )
  }

  def `classes can only extend one class or trait` = {
    check(
      Test.success(
        Traits.extendsFromOneTraits _,
        "An unfortunate woodchuck stampede occurred" :: HNil
      )
    )
  }

  def `are polymorphic` = {
    check(
      Test.success(
        Traits.polymorphicTraits _,
        true :: true :: true :: true :: HNil
      )
    )
  }

  def `may contain default implementations and state` = {
    check(
      Test.success(
        Traits.implementedTraits _,
        1 :: 1 :: HNil
      )
    )
  }

  def `are instantiated before classes` = {
    check(
      Test.success(
        Traits.previouslyInstantiatedTraits _,
        "Creating C1;In T1: x=0;In T1: x=1;In C1: y=0;In C1: y=2;Created C1" :: HNil
      )
    )
  }

  def `are instantiated from left to right` = {
    check(
      Test.success(
        Traits.fromLeftToRightTraits _,
        "Creating C1;In T1: x=0;In T1: x=1;In T2: z=0;In T2: z=1;In C1: y=0;In C1: y=2;Created C1" :: HNil
      )
    )
  }

  def `instantiations will not happen twice` = {
    check(
      Test.success(
        Traits.duplicateInstantiationTraits _,
        "Creating C1;In T2: z=0;In T2: z=1;In T1: x=0;In T1: x=1;In C1: y=0;In C1: y=2;Created C1" :: HNil
      )
    )
  }

  def `diamond inheritance problem does not happen` = {
    check(
      Test.success(
        Traits.diamondOfDeathTraits _,
        "Creating C1;In T1: x=0;In T1: x=1;In T2: z=0;In T2: z=2;In T3: w=0;In T3: w=3;In C1: y=0;In C1: y=4;Created C1" :: HNil
      )
    )
  }
}
