package concepts.control_flow.loops.returns.unlabelled_return

/* kotlin program to demonstrate use of unlabelled return.*/

fun main() {
    printOddNumbers()
    println("labelled return")
}

fun printOddNumbers() {
    listOf<Int>(1,2,3,4,5,6,7,8,9,10).forEach {
        if (it % 2 == 0) {
            return
        }
        println(it)
    }
}