package exercises.ex02

/*
 * Exercise 02 — Variables          (lesson: lesson02_variables)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex02*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex02/Ex02.kt.
 */

interface Ex02 {
    /**
     * 02.1 — Split a number of seconds into (hours, minutes, seconds).
     * secondsToClock(3725) == Triple(1, 2, 5)    // 1 h 2 min 5 s
     * Hint: use `val`s, integer division `/` and remainder `%`.
     */
    fun secondsToClock(totalSeconds: Int): Triple<Int, Int, Int>

    /** 02.2 — Convert Celsius to Fahrenheit: F = C × 9 / 5 + 32. celsiusToFahrenheit(100.0) == 212.0 */
    fun celsiusToFahrenheit(celsius: Double): Double

    /**
     * 02.3 — Simulate a score with a `var`: start at 10, add [bonus], then double it,
     * then subtract 3. scoreAfter(5) == (10 + 5) * 2 - 3 == 27.
     * Write it with ONE `var` that you update step by step.
     */
    fun scoreAfter(bonus: Int): Int
}

object Ex02Exercise : Ex02 {
    override fun secondsToClock(totalSeconds: Int): Triple<Int, Int, Int> = TODO("02.1")
    override fun celsiusToFahrenheit(celsius: Double): Double = TODO("02.2")
    override fun scoreAfter(bonus: Int): Int = TODO("02.3")
}
