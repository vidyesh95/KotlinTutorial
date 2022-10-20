package concepts.classes_and_objects.classes

/**
 * Visibility modifiers : Classes, objects, interfaces, constructors, and functions, as well as properties and their
 * setters, can have visibility modifiers. Getters always have the same visibility as their properties.
 * There are four visibility modifiers in Kotlin: private, protected, internal, and public.
 * The default visibility is public.
 */
fun main() {
    val visibilityModifiers6 = VisibilityModifiers6()
    visibilityModifiers6.test()
}

private class VisibilityModifiers1() {}
internal class VisibilityModifiers2() {}
public class VisibilityModifiers3() {}

open class VisibilityModifiers4 { // Super class
    private val a = 1 // Available only in class
    protected val b = 2 // Available only in itself and sub class
    internal val c = 3 // Available inside module
    public val d = 4 // Available everywhere
}

class VisibilityModifiers6 : VisibilityModifiers4() { // Derived class
    // a is not visible
    // b is visible
    // c is visible
    // d is visible
    fun test() {
        // println(a)
        println(b)
        println(c)
        println(d)

        val visibilityModifiers4 = VisibilityModifiers4()
        // println(visibilityModifiers4.a)
        // println(visibilityModifiers4.b)
        println(visibilityModifiers4.c)
        println(visibilityModifiers4.d)
    }
}