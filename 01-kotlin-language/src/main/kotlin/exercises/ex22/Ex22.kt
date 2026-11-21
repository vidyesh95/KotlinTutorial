package exercises.ex22

/*
 * Exercise 22 — The type system          (lesson: lesson22_type_system)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex22/Ex22.kt.
 */

/** A point that should be usable as a HashMap/HashSet key. */
class Point(val x: Int, val y: Int)

interface Ex22 {
    /**
     * 22.1 — Add up every number hidden in a mixed list:
     * Int and Long values count as-is, Strings count if they parse as an Int, everything else is ignored.
     * listOf(1, "2", 3L, "x", 4.5, null) → 6
     */
    fun sumNumbers(values: List<Any?>): Long

    /**
     * 22.2 — Return the value at [key] cast to Int, or [default] if it is missing OR not an Int.
     * Must never throw. Hint: `as?` and `?:`.
     */
    fun intOrDefault(map: Map<String, Any?>, key: String, default: Int): Int

    /**
     * 22.3 — Return the value of [key], or throw IllegalArgumentException("missing <key>").
     * Hint: a helper with return type Nothing on the right of `?:`.
     */
    fun require(map: Map<String, String>, key: String): String

    /**
     * 22.4 — Two Points are "the same" when x and y match. Return true if a == b by that rule.
     * Hint: you can't change Point — compare the properties yourself.
     * Bonus thought: what would you need to add to Point so that `setOf(Point(1,2), Point(1,2)).size == 1`?
     */
    fun samePoint(a: Point, b: Point): Boolean
}

object Ex22Exercise : Ex22 {
    override fun sumNumbers(values: List<Any?>): Long = TODO("22.1")
    override fun intOrDefault(map: Map<String, Any?>, key: String, default: Int): Int = TODO("22.2")
    override fun require(map: Map<String, String>, key: String): String = TODO("22.3")
    override fun samePoint(a: Point, b: Point): Boolean = TODO("22.4")
}
