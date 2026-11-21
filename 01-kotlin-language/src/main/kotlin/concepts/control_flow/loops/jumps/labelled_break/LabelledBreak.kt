package concepts.control_flow.loops.jumps.labelled_break

/* Kotlin program demonstrating use of labelled break.*/
fun main() {
    loop@ for (i in 1..10) {
        for (j in 1..10) {
            if (j == 5) break@loop
            println("i = $i, j = $j")
        }
    }
}