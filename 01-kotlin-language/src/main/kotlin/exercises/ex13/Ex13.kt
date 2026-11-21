package exercises.ex13

/*
 * Exercise 13 — higher-order functions and inline          (lesson: lesson13_higher_order_and_inline)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex13/Ex13.kt.
 * Rule for 13.1–13.3: write a plain `for` loop — don't call the stdlib filter/map/fold.
 */

interface Ex13 {
    /** 13.1 — Keep only the items for which [keep] returns true. */
    fun <T> myFilter(items: List<T>, keep: (T) -> Boolean): List<T>

    /** 13.2 — Transform every item with [transform]. */
    fun <T, R> myMap(items: List<T>, transform: (T) -> R): List<R>

    /**
     * 13.3 — Return a function that runs [first], then feeds its result to [second].
     * andThen({ it + 1 }, { it * 10 })(4) == 50.
     */
    fun andThen(first: (Int) -> Int, second: (Int) -> Int): (Int) -> Int

    /**
     * 13.4 — Index of the first negative number, or -1.
     * Try it with `numbers.forEachIndexed { … }` and a non-local `return`.
     */
    fun firstNegativeIndex(numbers: List<Int>): Int
}

object Ex13Exercise : Ex13 {
    override fun <T> myFilter(items: List<T>, keep: (T) -> Boolean): List<T> = TODO("13.1")
    override fun <T, R> myMap(items: List<T>, transform: (T) -> R): List<R> = TODO("13.2")
    override fun andThen(first: (Int) -> Int, second: (Int) -> Int): (Int) -> Int = TODO("13.3")
    override fun firstNegativeIndex(numbers: List<Int>): Int = TODO("13.4")
}
