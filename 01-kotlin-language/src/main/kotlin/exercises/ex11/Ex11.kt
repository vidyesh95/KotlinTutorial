package exercises.ex11

/*
 * Exercise 11 — functions          (lesson: lesson11_functions)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex11/Ex11.kt.
 */

interface Ex11 {
    /**
     * 11.1 — Format money: formatPrice(12.5) == "12.50 EUR", formatPrice(3.0, "USD", 0) == "3 USD".
     * Note the default arguments. Hint: `"%.2f".format(Locale.ROOT, x)` (ROOT → always a dot, never a comma).
     */
    fun formatPrice(amount: Double, currency: String = "EUR", decimals: Int = 2): String

    /** 11.2 — Largest of any number of Ints, or null when called with none. */
    fun maxOfAll(vararg numbers: Int): Int?

    /** 11.3 — base^exponent using RECURSION (no loops, no Math.pow). exponent >= 0. */
    fun power(base: Long, exponent: Int): Long

    /**
     * 11.4 — n-th Fibonacci number (0, 1, 1, 2, 3, 5, …; fib(0) = 0). Must handle n = 90 instantly:
     * write a private `tailrec` helper with accumulators.
     */
    fun fibonacci(n: Int): Long
}

object Ex11Exercise : Ex11 {
    override fun formatPrice(amount: Double, currency: String, decimals: Int): String = TODO("11.1")
    override fun maxOfAll(vararg numbers: Int): Int? = TODO("11.2")
    override fun power(base: Long, exponent: Int): Long = TODO("11.3")
    override fun fibonacci(n: Int): Long = TODO("11.4")
}
