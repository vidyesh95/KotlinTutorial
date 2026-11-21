package concepts.control_flow.loops.jumps.unlabelled_continue

/* Kotlin program demonstrating use of unlabelled continue.*/
fun main() {
    for (i in 1..10) {
        if (i == 5) continue
        println(i)
    }
}