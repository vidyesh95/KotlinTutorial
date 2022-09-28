package concepts.types.type_conversions

fun main() {
    val a: Int = 1
    val b: Long = a.toLong()
    val c: String = b.toString()
    val d: Double = c.toDouble()
    val e: Char = b.toInt().toChar()
    println("$a is ${a::class.simpleName}")
    println("$b is ${b::class.simpleName}")
    println("$c is ${c::class.simpleName}")
    println("$d is ${d::class.simpleName}")
    println("$e is ${e::class.simpleName}")
    /*println(a)
    println(b)
    println(c)
    println(d)
    println(e)
    when (d::class.simpleName) {
        "Int" -> print("Inferred type is Int")
        "Long" -> print("Inferred type is Long")
        "String" -> print("Inferred type is String")
        "Float" -> print("Inferred type is Float")
        "Char" -> print("Inferred type is Char")
        else -> { // Note the block
            print("c is neither Float nor Double")
        }
    }*/

}