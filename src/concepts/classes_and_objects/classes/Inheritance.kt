package concepts.classes_and_objects.classes


/**
 * Inheritance : All classes in Kotlin have a common superclass, Any, which is the default superclass for a class with
 * no supertypes declared. Any has three methods: equals(), hashCode(), and toString(). Thus, these methods are defined
 * for all Kotlin classes. By default, Kotlin classes are final – they can't be inherited. To make a class inheritable,
 * mark it with the open keyword.
 */
/*fun main() {
    val dog = Dog()
    dog.breed = "Doberman"
    dog.color = "Gray"
    println(dog.breed)
    println(dog.color)
    dog.bark()
    dog.eat()

    val cat = Cat()
    cat.age = 3
    cat.color = "Gray"
    cat.meow()
    cat.eat()

    val animal = Animal()
    animal.color = "White"
    animal.eat()
}

// Super class
open class Animal {
    open var color: String = ""

    open fun eat() {
        println("Eating")
    }
}

// Derived class
class Dog : Animal() {
    var breed: String = ""

    override var color:String = "Red"

    fun bark() {
        println("Bark")
    }

    override fun eat() {
        super<Animal>.eat()
        println("Dog eating")
    }
}

// Derived class
class Cat : Animal() {
    var age: Int = -1

    fun meow() {
        println("Meow")
    }

    override fun eat() {
        println("Cat eating")
    }
}*/


/*
fun main() {
    val dog = Dog("Gray", "Hound")
}

// Super class
open class Animal(var color: String) {
    init {
        println("Init Dog color:$color")
    }
}

// Derived class
class Dog(color: String, var breed: String) : Animal(color) {
    init {
        println("Init Dog color:$color and breed:$breed")
    }
}*/

fun main() {
    val dog = Dog("Gray", "Hound")
}

// Super class
open class Animal {
    var color: String = ""
    constructor(color: String) {
        this.color = color
        println("From Animal color : $color")
    }
}

// Derived class
class Dog : Animal {
    var breed: String = ""
    constructor(color: String, breed: String) : super(color) {
        this.breed =breed
        println("From Dog color : $color and breed : $breed")
    }
}

