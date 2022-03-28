package basics.basic_syntax.ranges

/*
fun main() {
    val x = 10
    val y = 9
    if (x in 1..y + 1) {
        println("fits in range")
    }
}*/

/*fun main() {
    val list = listOf("a", "b", "c")
    if (-1 !in 0..list.lastIndex) {
        println("-1 is out of range")
    }
    if (list.size !in list.indices) {
        println(list.indices)
    }
}*/

/*
fun main() {
    for (x in 1..5) {
        print(x)
    }
}*/

fun main() {
    for (x in 1..10 step 2) {
        print(x)
    }
    println()
    for (x in 9 downTo 0 step 3) {
        print(x)
    }
}