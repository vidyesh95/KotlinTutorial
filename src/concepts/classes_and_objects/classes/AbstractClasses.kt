package concepts.classes_and_objects.classes

/**
 * Abstract classes : The role of abstract class is to just provide set of methods and properties without any value or
 * any method body.
 *
 * A class may be declared abstract, along with some or all of its members. An abstract member does
 * not have an implementation in its class. You don't need to annotate abstract classes or functions with open.
 *
 * We can override a non-abstract open member with an abstract one.
 *
 * Abstract properties are open in nature and dont have body.
 *
 * Derived class should override abstract method and properties present inside superclass
 *
 * We cannot create instance of abstract class
 */

fun main() {
    val abstractClasses = AbstractClasses()
    println("Name : ${abstractClasses.name}")
    abstractClasses.eat()
}

abstract class Personal {
    fun goToSchool() {} // A normal function which is public and final by default.
    open fun getHeight() {} // An open function ready to be overridden.
    abstract fun eat() // Abstract properties are open by default.
    abstract var name:String
}

class AbstractClasses : Personal() { // Subclass or derived class
    override fun eat() {
        println("Eat")
    }
    override var name: String = "person_name"
}