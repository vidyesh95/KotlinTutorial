package concepts.types.type_operators

fun main() {
    val a: Int = 1
    val b: Int = 2
    val c: Int = 3

    val sum = a + b + c
    println(sum)

    // Prefix and postfix increment and decrement
    var firstNum = 1
    var secNum = 1
    val prefixIncrement: Int = ++firstNum
    val postfixIncrement: Int = secNum++
    println("prefixIncrement: $prefixIncrement")
    println("postfixIncrement: $postfixIncrement")
}