package exercises.ex24

/*
 * Exercise 24 — Generic constraints          (lesson: lesson24_generic_constraints)
 * Replace every TODO(...) then run: ./gradlew :01-kotlin-language:exercises
 */
interface Ex24 {
    /** 24.1 — Keep [value] inside [min]..[max]: clamp(15, 0, 10) == 10. Don't use coerceIn — write the comparisons. */
    fun <T : Comparable<T>> clamp(value: T, min: T, max: T): T

    /** 24.2 — True when every element is <= the next one. Empty and single-element lists are sorted. */
    fun <T : Comparable<T>> isAscending(items: List<T>): Boolean

    /** 24.3 — Sum any kind of numbers as a Double: [1, 2.5, 3L] → 6.5. */
    fun <T : Number> sumAsDouble(items: List<T>): Double

    /** 24.4 — Swap keys and values. If two keys share a value, the LAST one wins. */
    fun <K : Any, V : Any> invert(map: Map<K, V>): Map<V, K>
}

object Ex24Exercise : Ex24 {
    override fun <T : Comparable<T>> clamp(value: T, min: T, max: T): T = TODO("24.1")
    override fun <T : Comparable<T>> isAscending(items: List<T>): Boolean = TODO("24.2")
    override fun <T : Number> sumAsDouble(items: List<T>): Double = TODO("24.3")
    override fun <K : Any, V : Any> invert(map: Map<K, V>): Map<V, K> = TODO("24.4")
}
