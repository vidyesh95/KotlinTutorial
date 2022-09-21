package concepts.functions

/*A member function is a function that is defined inside a class or object.*/

fun main() {

    class Sample {
        fun foo() {
            println("Foo")
        }
    }

    /*creates instance of class Sample and calls foo.*/
    Sample().foo()
}