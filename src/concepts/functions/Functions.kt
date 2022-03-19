package concepts.functions

/*fun main() {
    println("Sum of 1 and 2 is ${sum(1, 2)}")
}

fun sum(a: Int, b: Int): Int {
    return a + b
}*/


fun printSum(a: Int, b: Int): Unit {
    println("sum of $a and $b is ${a + b}")
}

fun main() {
    printSum(-3, 9)
}