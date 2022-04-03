package concepts.types.basic_types

/* Kotlin program demonstrating different types.*/
fun main() {
    // Integer types
    val byte: Byte = 1
    val short: Short = 2
    val int: Int = 3
    val long: Long = 1234_5678_9012_3456L

    // Floating point types
    val float: Float = 1.0f
    val double: Double = 2.7182818284
    val float2: Float = 2.7182818284f

    // Boolean types
    val boolean: Boolean = true
    val boolean2: Boolean = false

    // Character types
    val char: Char = 'a'
    val char2: Char = 'b'

    // String types
    val string: String = "Hello"
    val string2: String = "World"

    // Array types
    val array: Array<Int> = arrayOf(1, 2, 3)
    val array2: Array<Int> = arrayOf(4, 5, 6)

    // List types
    val list: List<Int> = listOf(1, 2, 3)
    val list2: List<Int> = listOf(4, 5, 6)

    // Set types
    val set: Set<Int> = setOf(1, 2, 3)
    val set2: Set<Int> = setOf(4, 5, 6)

    // Map types
    val map: Map<String, Int> = mapOf("one" to 1, "two" to 2, "three" to 3)
    val map2: Map<String, Int> = mapOf("four" to 4, "five" to 5, "six" to 6)

    // Tuple types
    val tuple: Pair<Int, String> = 1 to "one"
    val tuple2: Pair<Int, String> = 2 to "two"
    val tuple3: Pair<Int, String> = 3 to "three"

    // Nullable types
    val nullable: Int? = null
    val nullable2: Int? = 4

    // Bitwise types
    val bitwise: Int = 0b1010



    println(bitwise)
}