package exercises.ex10

/*
 * Exercise 10 — exceptions and Result          (lesson: lesson10_exceptions_and_result)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex10/Ex10.kt.
 */

/** Thrown by [Ex10.withdraw]. [missing] = how much money was lacking. */
class InsufficientFundsException(val missing: Int) : Exception("missing $missing")

interface Ex10 {
    /** 10.1 — a / b, or null when b is 0. Don't let the ArithmeticException escape. */
    fun safeDivide(a: Int, b: Int): Int?

    /**
     * 10.2 — Parse an age. Throw IllegalArgumentException (hint: `require` / `requireNotNull`)
     * if the text is not a whole number or not in 0..150.
     */
    fun parseAge(text: String): Int

    /**
     * 10.3 — Parse every text as Int. Success with all numbers, or a failure holding the
     * NumberFormatException of the first bad text. Hint: `runCatching`.
     */
    fun parseAll(texts: List<String>): Result<List<Int>>

    /**
     * 10.4 — New balance after withdrawing [amount].
     * amount <= 0 → IllegalArgumentException; amount > balance → [InsufficientFundsException].
     */
    fun withdraw(balance: Int, amount: Int): Int
}

object Ex10Exercise : Ex10 {
    override fun safeDivide(a: Int, b: Int): Int? = TODO("10.1")
    override fun parseAge(text: String): Int = TODO("10.2")
    override fun parseAll(texts: List<String>): Result<List<Int>> = TODO("10.3")
    override fun withdraw(balance: Int, amount: Int): Int = TODO("10.4")
}
