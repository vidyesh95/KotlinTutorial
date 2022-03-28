package concepts.control_flow.loops.`for`

/*
fun main() {
    val items = listOf("apple", "mangoes", "oranges")
    for (item in items) {
        println(item)
    }
}*/

fun main() {
    val items = listOf("apple", "mangoes", "oranges")
    for (index in items.indices) {
        println("item at $index is ${items[index]}")
    }
}
