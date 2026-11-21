package solutions.ex08

import exercises.ex08.Ex08
import exercises.ex08.Shape
import kotlin.math.PI

object Ex08Solution : Ex08 {
    override fun fizzBuzz(n: Int): String = when {
        n % 15 == 0 -> "FizzBuzz"
        n % 3 == 0 -> "Fizz"
        n % 5 == 0 -> "Buzz"
        else -> n.toString()
    }

    override fun bmiCategory(bmi: Double): String = when {
        bmi < 18.5 -> "underweight"
        bmi < 25 -> "normal"
        bmi < 30 -> "overweight"
        else -> "obese"
    }

    override fun statusText(code: Int): String = when (code) {
        200 -> "OK"
        201 -> "Created"
        204 -> "No Content"
        404 -> "Not Found"
        in 300..399 -> "Redirect"
        in 400..499 -> "Client Error"
        in 500..599 -> "Server Error"
        else -> "Unknown"
    }

    override fun area(shape: Shape): Double = when (shape) {
        is Shape.Circle -> PI * shape.radius * shape.radius
        is Shape.Rectangle -> shape.width * shape.height
        is Shape.Square -> shape.side * shape.side
    }
}
