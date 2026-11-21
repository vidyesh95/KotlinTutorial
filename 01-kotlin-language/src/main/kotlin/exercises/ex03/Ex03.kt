package exercises.ex03

/*
 * Exercise 03 — Numbers and types          (lesson: lesson03_numbers_and_types)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex03*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex03/Ex03.kt.
 */

interface Ex03 {
    /**
     * 03.1 — The exact average of two Ints. averageOf(3, 4) == 3.5.
     * ⚠️ Two traps: integer division, AND overflow — averageOf(Int.MAX_VALUE, Int.MAX_VALUE)
     * must be 2147483647.0, not a negative number. Hint: convert to Long or Double first.
     */
    fun averageOf(a: Int, b: Int): Double

    /** 03.2 — True when [n] is even. Works for negatives too: isEven(-4) == true. */
    fun isEven(n: Int): Boolean

    /**
     * 03.3 — Money as whole cents: roundToCents(19.999) == 2000L, roundToCents(0.125) == 13L.
     * Hint: kotlin.math.roundToLong().
     */
    fun roundToCents(amount: Double): Long

    /**
     * 03.4 — Permission flags: READ = 1, WRITE = 2, EXEC = 4, combined with `or`.
     * Return true when ALL bits of [permission] are set in [flags].
     * hasPermission(1 or 2, 2) == true, hasPermission(1, 2) == false, hasPermission(7, 1 or 4) == true.
     */
    fun hasPermission(flags: Int, permission: Int): Boolean
}

object Ex03Exercise : Ex03 {
    override fun averageOf(a: Int, b: Int): Double = TODO("03.1")
    override fun isEven(n: Int): Boolean = TODO("03.2")
    override fun roundToCents(amount: Double): Long = TODO("03.3")
    override fun hasPermission(flags: Int, permission: Int): Boolean = TODO("03.4")
}
