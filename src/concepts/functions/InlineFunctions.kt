package concepts.functions

/*
An inline function is declared with keyword inline.
The use of inline function enhances the performance of higher order function.
The inline function tells compiler to copy parameters and functions to the call site.*/

/*Inline saves new function object allocation.
Without word inline for global function, In kotlin bytecode it creates new instance.
Inline is not used for local function.
Avoid inlining large functions.
When using inline functions, you're not allowed to keep a reference to the functions passed as a parameter or pass it as
a different function.
If we have multiple functions as parameters and you only need to keep reference to on of them use "noinline" keyword
before that parameter. the compiler will create new function object only for that specific function.*/

inline fun inlineFunc(fn: () -> Unit) {
    fn()
    println("Code inside Inline Function.")
}

fun main() {
    inlineFunc { println("Calling inline function.") }
}