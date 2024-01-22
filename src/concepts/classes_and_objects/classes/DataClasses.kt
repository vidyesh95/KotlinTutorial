package concepts.classes_and_objects.classes

/**
 * Data classes are used to hold data/state.
 * For each data class, the compiler automatically generates additional member functions that allow you to print an
 * instance to readable output, compare instances, copy instances, and so on.
 * For each data class, the compiler generates:
 * 1. equals():Boolean/hashCode():Int pair,
 * 2. toString() of the form "User(name=John, age=42)",
 * 3. componentN() functions corresponding to the properties in their order of declaration,
 * 4. copy() function.
 * To ensure consistency and meaningful behavior of the generated code, data classes have to fulfill the following
 * requirements:
 * 1. The primary constructor needs to have at least one parameter;
 * 2. All primary constructor parameters need to be marked as val or var;
 * 3. Data classes cannot be abstract, open, sealed or inner;
 * Data classes are immutable by default.
 * To make a data class mutable, you need to use the var keyword in front of the property name.
 */
fun main() {
    var user = User("Alex", 25)
    println("User name is ${user.name} and age is ${user.age}.")
    user = user.copy(name = "John")
    println("User name is ${user.name} and age is ${user.age}.")
}

data class User(val name: String, val age: Int)