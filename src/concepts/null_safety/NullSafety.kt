package concepts.null_safety

fun main() {
    var favoriteActor: String? = "Sandra Oh"
    println(favoriteActor?.length)

    favoriteActor = null
    println(favoriteActor?.length)
    val lengthOfName = favoriteActor?.length ?: 0
    println("The number of characters in your favorite actor's name is $lengthOfName.")

    var number: Int? = 10
    println(number)

    number = null
    println(number)

    val nullableList: List<Int?> = listOf(1, 2, null, 4)
    val intList: List<Int> = nullableList.filterNotNull()
    println(intList)

    val x:Int?=3
    val y:Int?=2
    val result = y?.let { x?.plus(it) }
    println(result)
}