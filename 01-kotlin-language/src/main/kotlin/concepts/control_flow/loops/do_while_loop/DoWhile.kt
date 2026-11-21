package concepts.control_flow.loops.do_while_loop

fun main() {
    var i = 1
    val number = 7
    do {
        println("7 × $i = " + number * i)
        i++
    } while (i < 11)
}