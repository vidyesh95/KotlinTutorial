package concepts.control_flow.loops.jumps.unlabelled_break

/* Kotlin program demonstrating use of unlabelled break.*/
fun main() {
    var i = 0
    while (i < 5) {
        println("i = $i")
        i++
        break
    }
    println("i = $i")
}