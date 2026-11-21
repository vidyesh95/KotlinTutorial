package solutions.ex22

import exercises.ex22.Ex22
import exercises.ex22.Point

object Ex22Solution : Ex22 {
    override fun sumNumbers(values: List<Any?>): Long = values.sumOf { value ->
        when (value) {
            is Int -> value.toLong()
            is Long -> value
            is String -> value.toIntOrNull()?.toLong() ?: 0L
            else -> 0L
        }
    }

    override fun intOrDefault(map: Map<String, Any?>, key: String, default: Int): Int = map[key] as? Int ?: default

    override fun require(map: Map<String, String>, key: String): String = map[key] ?: missing(key)

    private fun missing(key: String): Nothing = throw IllegalArgumentException("missing $key")

    // Bonus answer: make Point a `data class` (or override equals AND hashCode).
    override fun samePoint(a: Point, b: Point): Boolean = a.x == b.x && a.y == b.y
}
