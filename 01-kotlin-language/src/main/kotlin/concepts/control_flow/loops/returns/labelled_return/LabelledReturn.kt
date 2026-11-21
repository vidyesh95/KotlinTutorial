package concepts.control_flow.loops.returns.labelled_return

/* kotlin program to demonstrate use of labelled return.*/

/**
 * The return@label syntax is used to specify which function among several nested ones the statement returns to.
 * */
fun main() {
    printOddNumbers()
    println("labelled return")
}

fun printOddNumbers() {
    listOf<Int>(1,2,3,4,5,6,7,8,9,10).forEach {
        if (it % 2 == 0) {
            return@forEach
        }
        println(it)
    }
}
