package basics.basic_syntax.collections

/*fun main() {
    val items = listOf("apple", "banana", "orange")
    for (item in items) {
        println(item)
    }
}*/

fun main() {
    val fruits = listOf("apple", "banana", "orange")
    fruits.filter { it.startsWith("a") }.sortedBy { it }.map { it.uppercase() }.forEach { println(it) }
}