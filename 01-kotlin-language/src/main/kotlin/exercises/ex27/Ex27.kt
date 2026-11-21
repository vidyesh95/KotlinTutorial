package exercises.ex27

/*
 * Exercise 27 — Type erasure and reified      (lesson: lesson27_reified_and_erasure)
 * Replace every TODO(...) then run: ./gradlew :01-kotlin-language:exercises
 *
 * Note: interfaces can't contain inline/reified functions, so this exercise uses
 * plain top-level functions. Don't use filterIsInstance — that's the point. 🙂
 */

/** 27.1 — The first element whose runtime type is T, or null. */
inline fun <reified T> firstOfType(items: List<Any?>): T? = TODO("27.1")

/** 27.2 — How many elements are of type T. */
inline fun <reified T> countOfType(items: List<Any?>): Int = TODO("27.2")

/**
 * 27.3 — True when [value] is a Map whose keys are ALL of type K and values ALL of type V.
 * (`value is Map<K, V>` won't compile — check the entries one by one.) An empty map counts as true.
 */
inline fun <reified K, reified V> isMapOf(value: Any?): Boolean = TODO("27.3")

/** 27.4 — Without reified: keep elements that are instances of [type]. */
fun <T> filterByClass(items: List<Any?>, type: Class<T>): List<T> = TODO("27.4")
