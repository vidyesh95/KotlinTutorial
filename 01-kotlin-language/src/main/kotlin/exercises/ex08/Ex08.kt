package exercises.ex08

/*
 * Exercise 08 — if and when          (lesson: lesson08_if_and_when)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex08/Ex08.kt.
 */

sealed interface Shape {
    data class Circle(val radius: Double) : Shape
    data class Rectangle(val width: Double, val height: Double) : Shape
    data class Square(val side: Double) : Shape
}

interface Ex08 {
    /** 08.1 — "FizzBuzz" if n divisible by 3 and 5, "Fizz" if by 3, "Buzz" if by 5, else the number as text. */
    fun fizzBuzz(n: Int): String

    /**
     * 08.2 — Use a `when` WITHOUT subject:
     * below 18.5 → "underweight", below 25 → "normal", below 30 → "overweight", otherwise "obese".
     */
    fun bmiCategory(bmi: Double): String

    /**
     * 08.3 — Use a `when` WITH subject, mixing values and ranges:
     * 200 "OK", 201 "Created", 204 "No Content", 404 "Not Found",
     * any other 300..399 "Redirect", other 400..499 "Client Error", 500..599 "Server Error", else "Unknown".
     */
    fun statusText(code: Int): String

    /** 08.4 — Area of a [Shape] (circle: π·r², use kotlin.math.PI). Use an exhaustive `when` — no `else`! */
    fun area(shape: Shape): Double
}

object Ex08Exercise : Ex08 {
    override fun fizzBuzz(n: Int): String = TODO("08.1")
    override fun bmiCategory(bmi: Double): String = TODO("08.2")
    override fun statusText(code: Int): String = TODO("08.3")
    override fun area(shape: Shape): Double = TODO("08.4")
}
