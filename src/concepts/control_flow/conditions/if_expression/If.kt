package concepts.control_flow.conditions.if_expression

fun main() {
    println("max of 7 and 9 is ${maxOf(7, 9)}")
}

fun maxOf(a: Int, b: Int): Int {
    return if (a > b) {
        a
    } else {
        b
    }
}