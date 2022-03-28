package concepts.control_flow.loops.labelled_continue

/* Kotlin program demonstrating use of labelled continue.*/
fun main() {
    var i = 0
    outer@ while (i < 10) {
        i++
        println("i = $i")
        if (i == 5) {
            println("i == 5")
            continue@outer
        }
    }
}