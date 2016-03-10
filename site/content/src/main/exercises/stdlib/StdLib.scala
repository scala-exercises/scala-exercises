/*
 * scala-exercises-content
 * Copyright (C) 2015-2016 47 Degrees, LLC. <http://www.47deg.com>
 */

package stdlib

/** Scala fuses object-oriented and functional programming in a statically typed programming language.
  *
  * @param name std_lib
  */
object StdLib extends exercise.Library {

  override def color = Some("#dc322f")

  override def sections = List(
    Asserts,
    ValandVar,
    Classes,
    Options,
    Objects,
    Tuples,
    HigherOrderFunctions,
    Lists,
    Maps,
    Sets,
    Formatting,
    PatternMatching,
    CaseClasses,
    RangeExercises,
    PartiallyAppliedFunctions,
    PartialFunctions,
    Implicits,
    Traits,
    ForExpressions,
    InfixPrefixandPostfixOperators,
    InfixTypes,
    MutableMaps,
    MutableSets,
    SequencesandArrays,
    Iterables,
    Traversables,
    NamedandDefaultArguments,
    Preconditions,
    Extractors,
    ByNameParameter,
    RepeatedParameters,
    ParentClasses,
    EmptyValues,
    TypeSignatures,
    UniformAccessPrinciple,
    LiteralBooleans,
    LiteralNumbers,
    LiteralStrings,
    TypeVariance,
    Enumerations,
    Constructors
  )
}
