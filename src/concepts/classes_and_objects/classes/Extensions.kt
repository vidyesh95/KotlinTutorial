package concepts.classes_and_objects.classes

/**
 * Extensions : Kotlin provides the ability to extend a class or an interface with new functionality without having to
 * inherit from the class or use design patterns such as Decorator. This is done via special declarations called
 * extensions.
 *
 * Extension functions : We can write new functions for a class or an interface from a third-party library that you
 * can't modify. Such functions can be called in the usual way, as if they were methods of the original class.
 * This mechanism is called an extension function.
 * The role of extension function is to add new function to the classes. We can add new functions to a class
 * without declaring it. The new functions added behaves like static.
 *
 * Extension properties : Extension properties lets us define new properties for existing classes.
 */

fun main() {
val extension = Extension()
    println("Extension function : ${extension.isExtended(points = 31)}")
    println("Extension property : ${extension.isGood(points = 97)}")
}

class Extension {
    fun isExtended(points:Int) : Boolean {
        return points > 30
    }
}

fun Extension.isGood(points:Int) : Boolean {
    return points > 90
}