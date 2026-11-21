package concepts.control_flow.conditions.when_expression

/*
fun main() {
    val x = 1
    when (x) {
        1 -> println("x == 1")
        2 -> println("x == 2")
        else -> {
            println("x is neither 1 nor 2")
        }
    }
}*/

fun main() {
    val (number1, operator, number2) = readLine()!!.split(" ")
    val a: Int = number1.toInt()
    val b: Int = number2.toInt()
    when (operator) {
        "+" -> println(a + b)
        "-" -> println(a - b)
        "*" -> println(a * b)
        "/" -> println(a / b)
        else -> println("Unknown operator")
    }
}