package concepts.functions

/*Kotlin allows us to provide custom implementations for the predefined set of operators on types.
To overload an operator, mark the corresponding function with the "operator" modifier.*/

data class Point(val x: Int, val y: Int)

operator fun Point.unaryMinus() = Point(-x, -y)

val point = Point(10, 20)

fun main() {
    println(-point)
}