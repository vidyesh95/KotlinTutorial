package basics.basic_syntax.type_checks_and_automatic_casts

fun main() {
    fun printLength(obj: Any) {
        println("Getting length of `$obj`. \nLength is ${getStringLength(obj) ?: "Error: The object is not a string"}\n")
    }

    printLength("Incomprehensibility")
    printLength(1000)
    printLength(listOf(Any()))
    printLength("")
}

fun getStringLength(obj: Any): Int? {
    // `obj` is automatically cast to `String` in this branch
    if (obj is String && obj.length > 0) {
        return obj.length
    }

    return null
}