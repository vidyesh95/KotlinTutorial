package solutions.ex24

import exercises.ex24.Ex24

object Ex24Solution : Ex24 {
    override fun <T : Comparable<T>> clamp(value: T, min: T, max: T): T = when {
        value < min -> min
        value > max -> max
        else -> value
    }

    override fun <T : Comparable<T>> isAscending(items: List<T>): Boolean =
        items.zipWithNext().all { (a, b) -> a <= b }

    override fun <T : Number> sumAsDouble(items: List<T>): Double = items.sumOf { it.toDouble() }

    override fun <K : Any, V : Any> invert(map: Map<K, V>): Map<V, K> =
        map.entries.associate { (key, value) -> value to key }
}
