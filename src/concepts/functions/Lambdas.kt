package concepts.functions

import kotlin.concurrent.thread

/*Lambda is a function which has no name.
Lambda is defined with a curly braces {} which takes variable as a parameter(if any) and body of function.
The body of function is written after variable(if any) followed by -> operator.*/
/*Syntax*/
/*val lambda_name : Data_type = { argument_list -> code_body }*/
/*with type annotation in lambda expression
val sum = {a: Int , b: Int -> a + b}*/
/*without type annotation in lambda expression
val sum:(Int,Int) -> Int = { a, b -> a + b}*/
/*val sum: (Int, Int) -> Int = { x: Int, y: Int -> x + y }*/

fun rollDice(
    range: IntRange,
    time: Int,
    callback: (result: Int) -> Unit
) {
    for (i in 0 until time) {
        val result = range.random()
        callback(result)
    }
}

fun rollDice(callback: ((result: Int) -> Unit)? = null): String {
    thread {
        Thread.sleep(3000)
        callback?.invoke(4)
    }
    return "Dice"
}

fun add(a: Double, b: Double): Double {
    return a + b
}

fun higherOrderFunc(a: Double, b: Double, func: (Double, Double) -> Double): Double {
    println(func(a, b))
    return func(a, b)
}


fun main() {
    /*rollDice(
        range = 1..6,
        time = 4,
        callback = { result ->
            println(result)
        }
    )*/

    /*val result = rollDice() {
        println(it)
    }
    println(result)*/

    /*val modOperation = { a: Int -> a % 2 == 0 }
    println(modOperation(4))*/

    val output = higherOrderFunc(2.0, 3.0) { a: Double, b: Double -> a + b }
    println("Output $output")
}