package shapelessex

/** Shapeless is a type class and dependent type based generic programming library for Scala.
  *
  * @param name shapeless
  */
object ShapelessLib extends exercise.Library {
  override def color = Some("#6573C4")

  override def sections = List(
    PolyExercises,
    HListExercises,
    TuplesExercises,
    ArityExercises,
    HMapExercises,
    SingletonExercises,
    ExtensibleRecordsExercises,
    CoproductExercises,
    GenericExercises,
    LensesExercises,
    AutoTypeClassExercises,
    LazyExercises,
    SizedExercises,
    TypeSafeCastExercises
  )
}
