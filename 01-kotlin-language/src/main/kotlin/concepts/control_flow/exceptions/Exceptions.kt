package concepts.control_flow.exceptions

import java.lang.IndexOutOfBoundsException

class IllegalIdException(message : String) : Exception()

fun id(name: String, age: Int) {
    if (age < 18) {
        throw IllegalIdException(message = "Younger than 18")
    }
    println("$name passed")
}

fun main() {
    try {
        id(name = "abra", age = 17)
    } catch (e: IllegalIdException) {
        println(e.message)
        //e.printStackTrace()
    } catch (e: Exception) {
        println(e.message)
    }

    val a = 10
    val b = 0

    val names = listOf<String>("Striker")

    try {
        println(a / b)
    } catch (e: ArithmeticException) {
        println(e.message)
    } finally {
        println(b / a)
    }

    try {
        println(names[1])
    } catch (e: IndexOutOfBoundsException) {
        println(e.message)
    }

}