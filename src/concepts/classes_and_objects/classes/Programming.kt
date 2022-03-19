package concepts.classes_and_objects.classes

class Programming(val name: String) {
    val funName = "I like to program in $name"
}

fun main() {
    val c = Programming(name = "kotlin")
    println(c.funName)
}