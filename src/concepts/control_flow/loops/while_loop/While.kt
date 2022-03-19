package concepts.control_flow.loops.while_loop

/*fun main() {
    var i = 0
    while (i < 10) {
        println(i)
        i++
    }
}*/

fun main() {
    val items = listOf("apple", "mango", "orange")
    var index = 0
    while (index < items.size) {
        println("item at $index is ${items[index]}")
        index++
    }
}