package exercises.ex20

/*
 * Exercise 20 — Extensions and operators          (lesson: lesson20_extensions_and_operators)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex20/Ex20.kt.
 * Challenge: solve each task by FIRST writing an extension or operator function
 * (see the hints), then calling it from the object below.
 */

/** A fraction like 1/2. Always has a positive denominator. */
data class Fraction(val numerator: Int, val denominator: Int) {
    init {
        require(denominator > 0) { "denominator must be positive" }
    }

    override fun toString() = "$numerator/$denominator"
}

interface Ex20 {
    /**
     * 20.1 — "hello big world" → "Hello Big World" (words separated by single spaces).
     * Hint: `fun String.capitalizeWords(): String`.
     */
    fun capitalizeWords(text: String): String

    /**
     * 20.2 — Add two fractions and REDUCE the result: 1/2 + 1/3 = 5/6, 1/4 + 1/4 = 1/2.
     * Hint: `operator fun Fraction.plus(other: Fraction): Fraction` and a gcd helper.
     */
    fun add(a: Fraction, b: Fraction): Fraction

    /**
     * 20.3 — Compare two fractions by value: negative if a < b, 0 if equal (1/2 vs 2/4), positive if a > b.
     * Hint: cross-multiply with Long to avoid overflow; an `operator fun Fraction.compareTo` lets you write `a < b`.
     */
    fun compare(a: Fraction, b: Fraction): Int
}

object Ex20Exercise : Ex20 {
    override fun capitalizeWords(text: String): String = TODO("20.1")
    override fun add(a: Fraction, b: Fraction): Fraction = TODO("20.2")
    override fun compare(a: Fraction, b: Fraction): Int = TODO("20.3")
}
