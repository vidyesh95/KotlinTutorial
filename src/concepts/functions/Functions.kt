package concepts.functions

/*fun main() {
    println("Sum of 1 and 2 is ${sum(1, 2)}")
}

fun sum(a: Int, b: Int): Int {
    return a + b
}*/


/*Unit means the function doesn't return a value.*/
/*
fun printSum(a: Int, b: Int): Unit {
    println("sum of $a and $b is ${a + b}")
}

fun main() {
    printSum(-3, 9)
}*/

fun main() {
    /*named arguments*/
    println(birthdayGreeting(name = "Vidyesh", age = 26))
}

/*default argument*/
fun birthdayGreeting(name: String = "Alex", age: Int = 20): String {
    val nameGreeting = "Happy Birthday, $name!"
    val ageGreeting = "You are now $age years old!"
    return "$nameGreeting\n$ageGreeting"
}