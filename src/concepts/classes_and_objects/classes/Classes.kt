package concepts.classes_and_objects.classes

import kotlin.math.abs

/**
 * Class : A specification or blueprint for an object. example: class Car.
 * Object : A physical presence of the class in memory. example: class car = Car("Audi")
 * Instance : An unique copy of the object at a particular time. example: if(car is String)
 *            alternatively parameter of class at particular time like var audi = Car(color: "white", make: "VW")
 *            var mercedes = Car(color: "orange", make: "Mercedes")
 *            Object and instance can be used interchangeably
 * A class consist of three parts:
 * Properties : Variables that specify the attributes of the class's objects.
 * Methods : Functions that contain the class's behaviors and actions.
 * Constructors : A special member function that creates instances of the class throughout the program in which it's
 *                defined.*/
fun main() {
    val person = Person()
    val person1 = Person()
    println("Class name is ${person::class.simpleName}.")
    println("Full name is ${person.firstName} ${person.lastName} and gender is ${person.gender}.")

    person1.firstName = "Jenny"
    person1.gender = "Female"
    println("Full name is ${person1.firstName} ${person1.lastName} and gender is ${person1.gender}.")
    person1.personWalking()
    person1.isHealthy()
}

class Person {
    var firstName = "Alex"
    val lastName = "Truman"
    var gender = "Male"

    fun personWalking() {
        println("Person is walking.")
    }

    fun isHealthy() {
        println("Person is healthy.")
    }
}

/**
 * Primary constructor : A class can have a primary constructor and one or more secondary constructors.
 * The primary constructor is a part of the class header and it goes after the class name and optional type parameters.
 * If the primary constructor does not have any annotations or visibility modifiers, the constructor keyword can be
 * omitted.
 * The primary constructor cannot contain any code. Initialization code can be placed in initializer blocks prefixed
 * with the init keyword.
 * During the initialization of an instance, the initializer blocks are executed in the same order as they appear in the
 * class body, interleaved with the property initializers.
 */
/*fun main() {
    val person = Person("Alex", "Truman", "Male")
    println("Class name is ${person::class.simpleName}.")
    println("Full name is ${person.firstName} ${person.lastName} and gender is ${person.gender}.")
}

// 'constructor' keyword optional.
class Person constructor(val firstName: String, val lastName: String, val gender: String) {
    init {
        println("Initialization code.")
    }
}*/

/**
 * Secondary constructor : A class can also declare secondary constructors, which are prefixed with constructor.
 */
/*fun main() {
    val person = Person("Vidyesh", "Churi", 10)

    println("Main : Full name of person is ${person.firstName} ${person.lastName}.")

    println("Main : ${person.id}")
}

class Person(val firstName: String, val lastName: String) {
    var id: Int = -1

    init {
        println("Class : Full name of person is $firstName $lastName.")
    }

    constructor(_firstName: String, _lastName: String, _id: Int) : this(_firstName, _lastName) {
        // The body of secondary constructor is called after init block in primary constructor
        this.id = _id
    }
}*/

/**
 * Getters and setters.
 * */
/*
fun main() {
    val person = Person(firstName = "Vidyesh", lastName =  "Churi")
    person.age = -15
    println("Main : Full name of person is ${person.firstName} ${person.lastName} of length ${person.length} and age ${person.age}.")
}

class Person(val firstName: String, val lastName: String) {

    val length: Int
        get() = firstName.length + lastName.length

    var age: Int = 0
        set(value) {
            if (value < 0) {
                println("Input age is $value and age cannot be negative.")
                field = abs(value)
            } else {
                field = value
            }
        }

    init {
        println("Class : Full name of person is $firstName $lastName.")
    }
}*/
