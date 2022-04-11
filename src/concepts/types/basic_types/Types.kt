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

    // Binary literal constants
    val binary: Int = 0b1010

    // Hexadecimal literal constants
    val hexadecimal: Int = 0x0ADF9C

    // Unary plus and minus
    val unaryPlus: Int = +1
    val unaryMinus: Int = -1

    // Unsigned types
    val ubyte: UByte = 1u
    val ushort: UShort = 2u
    val uint: UInt = 3u
    val ulong: ULong = 1234_5678_9012_3456u

    // Boolean types
    val myTrue: Boolean = true
    val myFalse: Boolean = false
    val boolNull: Boolean? = null

    println(myTrue || myFalse)
    println(myTrue && myFalse)
    println(!myTrue)

    // Character types
    val aChar: Char = 'a'

    println(aChar)
    println('\n') //prints an extra newline character
    println('\uFF00')

    // Array types
    val arrayOfInts: Array<Int> = arrayOf(1, 2, 3)
    val arrayOfStrings: Array<String> = arrayOf("one", "two", "three")
    val arrayOfChars: Array<Char> = arrayOf('a', 'b', 'c')
    val arrayOfBooleans: Array<Boolean> = arrayOf(true, false, true)
    val arrayOfNulls: Array<Int?> = arrayOf(null, null, null)

    // List types
    val listOfInts: List<Int> = listOf(1, 2, 3)
    val listOfStrings: List<String> = listOf("one", "two", "three")
    val listOfChars: List<Char> = listOf('a', 'b', 'c')
    val listOfBooleans: List<Boolean> = listOf(true, false, true)
    val listOfNulls: List<Int?> = listOf(null, null, null)

    // Set types
    val setOfInts: Set<Int> = setOf(1, 2, 3)
    val setOfStrings: Set<String> = setOf("one", "two", "three")
    val setOfChars: Set<Char> = setOf('a', 'b', 'c')
    val setOfBooleans: Set<Boolean> = setOf(true, false, true)
    val setOfNulls: Set<Int?> = setOf(null, null, null)

    // Map types
    val mapOfInts: Map<String, Int> = mapOf("one" to 1, "two" to 2, "three" to 3)
    val mapOfStrings: Map<String, String> = mapOf("one" to "one", "two" to "two", "three" to "three")
    val mapOfChars: Map<String, Char> = mapOf("one" to 'a', "two" to 'b', "three" to 'c')
    val mapOfBooleans: Map<String, Boolean> = mapOf("one" to true, "two" to false, "three" to true)
    val mapOfNulls: Map<String, Int?> = mapOf("one" to null, "two" to null, "three" to null)

    // Tuple types
    val tupleOfInts: Pair<Int, String> = 1 to "one"
    val tupleOfStrings: Pair<String, String> = "one" to "one"
    val tupleOfChars: Pair<Char, Char> = 'a' to 'a'
    val tupleOfBooleans: Pair<Boolean, Boolean> = true to false
    val tupleOfNulls: Pair<Int?, Int?> = null to null

    // Nullable types
    val nullableInt: Int? = null
    val nullableString: String? = null
    val nullableChar: Char? = null
    val nullableBoolean: Boolean? = null
    val nullableArray: Array<Int?> = arrayOf(null, null, null)
    val nullableList: List<Int?> = listOf(null, null, null)
    val nullableSet: Set<Int?> = setOf(null, null, null)
    val nullableMap: Map<String, Int?> = mapOf("one" to null, "two" to null, "three" to null)
    val nullableTuple: Pair<Int?, String?> = null to null

    arrayOfStrings.forEach { println(it) }

    println(boolNull)
}