package concepts.functions

/*
Higher order function is a function that takes functions as parameters or returns a function.
We can pass a function as a parameter in other function instead of passing Int, String and other types as a parameter in
other function*/

fun add(a: Int, b: Int): Int {
    return a + b
}

fun higherOrderFunction(a: Int, b: Int, func: (Int, Int) -> Int) {
    println(func(a, b))
}

fun main() {
    val output = higherOrderFunction(2, 3, ::add)
    println("Output $output")
}