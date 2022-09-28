package concepts.classes_and_objects.classes

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
    println(person::class.simpleName)
}

class Person {

}