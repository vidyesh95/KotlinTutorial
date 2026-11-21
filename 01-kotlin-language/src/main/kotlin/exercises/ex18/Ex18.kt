package exercises.ex18

/*
 * Exercise 18 — Sealed hierarchies          (lesson: lesson18_sealed_hierarchies)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex18/Ex18.kt.
 * Rule for this exercise: use `when` WITHOUT `else` on every sealed type.
 */

sealed interface Shape {
    data class Circle(val radius: Double) : Shape
    data class Rectangle(val width: Double, val height: Double) : Shape
    data class Triangle(val base: Double, val height: Double) : Shape
}

sealed interface Command {
    data class Deposit(val amount: Long) : Command
    data class Withdraw(val amount: Long) : Command
    data object Reset : Command
}

sealed interface ParseResult {
    data class Ok(val value: Int) : ParseResult
    data class Error(val message: String) : ParseResult
}

interface Ex18 {
    /** 18.1 — Area of any shape. Circle: π·r², Rectangle: w·h, Triangle: base·height / 2. */
    fun area(shape: Shape): Double

    /**
     * 18.2 — Start from [start] and apply the commands in order.
     * Deposit adds; Withdraw subtracts ONLY if the balance is big enough (otherwise skip it);
     * Reset sets the balance to 0. Return the final balance.
     */
    fun applyAll(start: Long, commands: List<Command>): Long

    /**
     * 18.3 — Parse an age. Not an integer → Error("not a number").
     * Integer outside 0..150 → Error("out of range"). Otherwise Ok(value).
     */
    fun parseAge(text: String): ParseResult

    /** 18.4 — Ok(30) → "age=30", Error("x") → "error: x". */
    fun render(result: ParseResult): String
}

object Ex18Exercise : Ex18 {
    override fun area(shape: Shape): Double = TODO("18.1")
    override fun applyAll(start: Long, commands: List<Command>): Long = TODO("18.2")
    override fun parseAge(text: String): ParseResult = TODO("18.3")
    override fun render(result: ParseResult): String = TODO("18.4")
}
