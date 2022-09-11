package basics.basic_syntax.nullable_values_and_null_checks

/*
fun main(args: Array<String>) {
    val s: String? = "This shouldn't be null"
    println(s?.length)
    println(s?.get(0))
}*/

/*fun main() {
    printProduct("6", "7")
    printProduct("6", "")
    printProduct("a", "7")
    printProduct("99", "b")
    printProduct("a", "b")
}

fun printProduct(arg1: String, arg2: String) {
    val x = arg1.toIntOrNull()
    val y = arg2.toIntOrNull()

    // If x or y is null, the corresponding variable is not assigned
    // and Kotlin knows that the product of x and y is definitely null.
    if (x == null && y == null) {
        println("'$arg1' and '$arg2' is not a number")
        return
    }
    if (x == null) {
        println("'$arg1' is not a number")
        return
    }
    if (y == null) {
        println("'$arg2' is not a number")
        return
    }

    println(x * y)
}*/
fun nullable(nullableString: String?, default: Int): Int {
    return if (nullableString?.length == 0) {
        default
    } else {
        nullableString?.length!! // !! is called the 'not-null assertion operator'
    }
}

fun main(args: Array<String>) {
    val str = readlnOrNull()
    val result = -1
    println(nullable(str, result))
}