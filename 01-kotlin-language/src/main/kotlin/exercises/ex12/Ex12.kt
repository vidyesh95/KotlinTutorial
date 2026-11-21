package exercises.ex12

/*
 * Exercise 12 — lambdas and function types          (lesson: lesson12_lambdas_and_function_types)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex12/Ex12.kt.
 */

/** A price rule. A `fun interface`, so a lambda can implement it. */
fun interface Discount {
    fun apply(price: Double): Double
}

interface Ex12 {
    /** 12.1 — Call [f] on [x], then call [f] again on that result. applyTwice({ it + 3 }, 1) == 7. */
    fun applyTwice(f: (Int) -> Int, x: Int): Int

    /** 12.2 — Return a FUNCTION that adds [n] to its argument. makeAdder(5)(10) == 15. */
    fun makeAdder(n: Int): (Int) -> Int

    /** 12.3 — Return a counter function: each call returns 1, 2, 3, … Two counters must be independent (closures!). */
    fun counter(): () -> Int

    /** 12.4 — A [Discount] that takes [percent] % off: percentOff(20).apply(50.0) == 40.0. Use a lambda (SAM conversion). */
    fun percentOff(percent: Int): Discount
}

object Ex12Exercise : Ex12 {
    override fun applyTwice(f: (Int) -> Int, x: Int): Int = TODO("12.1")
    override fun makeAdder(n: Int): (Int) -> Int = TODO("12.2")
    override fun counter(): () -> Int = TODO("12.3")
    override fun percentOff(percent: Int): Discount = TODO("12.4")
}
