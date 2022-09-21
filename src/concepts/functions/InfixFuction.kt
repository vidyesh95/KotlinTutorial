package concepts.functions

/*You can make a function call in kotlin using infix notation if the function is a member function or an extension
function, has only one parameter and is marked with "infix" keyword.*/

/*"infix" function calls have lower precedence than arithmetic operators, type casts, and range to operator.*/

infix fun Int.add(b: Int): Int = this + b

fun main() {
    /*var x = 10.add(20)*/
    var x = 10 add 20
    println(x)
}