/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 11 — Functions
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : declare functions, use default & named arguments, single-expression
 *         bodies, `Unit`, `vararg`, local functions, member functions, and
 *         recursion (+ `tailrec`) — and see the call stack in your head.
 *  Needs: lessons 08–10
 *  Time : ~35 min
 *  Next : lesson 12 — lambdas and function types
 *  Exercises: exercises/ex11/Ex11.kt
 */
package lesson11_functions

fun main() {
    section1Anatomy()
    section2DefaultAndNamedArguments()
    section3UnitAndSingleExpression()
    section4Vararg()
    section5LocalAndMemberFunctions()
    section6RecursionAndTheCallStack()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Anatomy of a function
// ───────────────────────────────────────────────────────────────────────────
//   fun  sum ( a: Int, b: Int ) : Int  { return a + b }
//   ───  ───   ──────────────    ───    ──────────────
//   kw   name  parameters        return  body
//                (name: Type)    type
//
// 🧒 Like I'm 5:
//    A function is a vending machine. You put things IN (parameters: coins and
//    a button number), it does its work inside where you can't see, and gives
//    you something OUT (return value: a snack). Same inputs → same snack.
//
// Parameters are read-only (`val`). Every parameter needs a type.

fun sum(a: Int, b: Int): Int {
    return a + b
}

fun section1Anatomy() {
    println("§1")
    println("Sum of 1 and 2 is ${sum(1, 2)}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Default and named arguments
// ───────────────────────────────────────────────────────────────────────────
// Give a parameter a default (`= value`) and callers may skip it.
// Call with `name = value` to make calls readable and to skip middle params.
// This replaces the many "overloads" you'd write in Java.

fun birthdayGreeting(name: String = "Alex", age: Int = 20): String =
    "Happy Birthday, $name! You are now $age years old!"

fun connect(host: String, port: Int = 5432, ssl: Boolean = true, timeoutSeconds: Int = 30): String =
    "connect($host:$port, ssl=$ssl, timeout=${timeoutSeconds}s)"

fun section2DefaultAndNamedArguments() {
    println("§2")
    println(birthdayGreeting())
    println(birthdayGreeting("Vidyesh", 26))
    println(birthdayGreeting(age = 30))                       // skip `name`, set `age`
    println(connect("localhost"))
    println(connect("db.internal", timeoutSeconds = 5))       // named: no guessing what `5` means
    // connect("db", 5432, true, 5)  ← compiles, but what's `true`? what's `5`? Prefer names.
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `Unit` and single-expression functions
// ───────────────────────────────────────────────────────────────────────────
// A function that returns "nothing useful" returns `Unit` (like Java's void,
// but it's a real object). You may omit `: Unit`.
//
// If the body is ONE expression, drop the braces and `return`:
//     fun square(x: Int) = x * x         // return type Int inferred
// 🆕 Kotlin 2.3+: if you DO declare the return type, you may also use `return`
// inside such an expression body (e.g. inside a `when` branch).

fun printSum(a: Int, b: Int): Unit {
    println("sum of $a and $b is ${a + b}")
}

fun square(x: Int) = x * x

fun sign(x: Int): String = when {
    x > 0 -> "positive"
    x < 0 -> "negative"
    else -> return "zero"                 // 🆕 return allowed in expression body with explicit type
}

fun section3UnitAndSingleExpression() {
    println("§3")
    printSum(-3, 9)
    val nothingUseful: Unit = printSum(1, 1)
    println(nothingUseful)                // prints "kotlin.Unit"
    println(square(12))
    println(listOf(5, -5, 0).map(::sign))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. vararg — "any number of arguments"
// ───────────────────────────────────────────────────────────────────────────
// Inside the function a `vararg x: T` is an Array<out T>. To pass an existing
// array, spread it with `*`.

fun average(vararg numbers: Int): Double =
    if (numbers.isEmpty()) 0.0 else numbers.sum().toDouble() / numbers.size

fun section4Vararg() {
    println("§4")
    println(average())
    println(average(1, 2, 3, 4))
    val scores = intArrayOf(10, 20)
    println(average(*scores, 30))         // spread operator `*`
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Local functions and member functions
// ───────────────────────────────────────────────────────────────────────────
// A LOCAL function lives inside another function: a private helper nobody
// else can call. It can read the outer function's variables.
// A MEMBER function lives inside a class and works on that object's data.

fun validatePassword(password: String): List<String> {
    val problems = mutableListOf<String>()
    fun rule(ok: Boolean, message: String) {          // local helper
        if (!ok) problems += message                  // uses outer `problems`
    }
    rule(password.length >= 8, "at least 8 characters")
    rule(password.any(Char::isDigit), "at least one digit")
    rule(password.any(Char::isUpperCase), "at least one uppercase letter")
    return problems
}

class Counter {
    private var count = 0
    fun increment(by: Int = 1): Counter {             // member function
        count += by
        return this                                   // returning `this` allows chaining
    }

    fun current() = count
}

fun section5LocalAndMemberFunctions() {
    println("§5")
    println(validatePassword("hunter2"))
    println(validatePassword("Correct-Horse-9"))
    println(Counter().increment().increment(by = 5).current())
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Recursion and the call stack
// ───────────────────────────────────────────────────────────────────────────
// A recursive function calls itself on a SMALLER problem until it hits a
// base case that needs no further call.
//
// 🧒 Like I'm 5 — the call stack:
//    Every function call is a sticky note put on top of a pile: "I'm working
//    on factorial(3), waiting for factorial(2)". Then a new note on top for
//    factorial(2), then factorial(1). factorial(1) knows the answer (1), so
//    its note is removed and the answer passed down to the note below, and so on.
//
//      factorial(1) = 1          ← top, finishes first
//      factorial(2) = 2 * …
//      factorial(3) = 3 * …
//      main()                     ← bottom
//
//    Too many notes (e.g. recursing 100_000 deep) → the pile falls over:
//    StackOverflowError.
//
// `tailrec`: if the recursive call is the VERY LAST thing the function does,
// mark it `tailrec` and the compiler rewrites it into a loop — no pile grows.

fun factorial(n: Int): Long = if (n <= 1) 1 else n * factorial(n - 1)

fun factorialTraced(n: Int, depth: Int = 0): Long {
    val indent = "  ".repeat(depth)
    println("${indent}→ factorial($n)")
    val result = if (n <= 1) 1L else n * factorialTraced(n - 1, depth + 1)
    println("${indent}← factorial($n) = $result")
    return result
}

tailrec fun sumUpTo(n: Long, accumulator: Long = 0): Long =
    if (n == 0L) accumulator else sumUpTo(n - 1, accumulator + n)   // last action = the call

fun section6RecursionAndTheCallStack() {
    println("§6")
    println(factorial(10))
    factorialTraced(3)
    println(sumUpTo(1_000_000))           // 1 million deep would overflow without tailrec
}

// ⚠️ Gotcha: a recursive function without a reachable base case never stops
// and ends in StackOverflowError. Always write the base case first.

// 🏗️ In the real world: Spring controller methods and service methods are just
// member functions; default + named args make Kotlin service APIs pleasant
// (`findBooks(author = "x", limit = 10)`).

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `fun name(p: Type): Return { … }`; one-expression bodies use `=`.
//  • Defaults + named arguments replace overloads and make calls self-explaining.
//  • `Unit` = no meaningful result; `vararg` + `*spread` for many args.
//  • Local functions = private helpers; member functions live in classes.
//  • Each call pushes a frame on the call stack; `tailrec` turns tail recursion into a loop.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Sum of 1 and 2 is 3
// §2
// Happy Birthday, Alex! You are now 20 years old!
// Happy Birthday, Vidyesh! You are now 26 years old!
// Happy Birthday, Alex! You are now 30 years old!
// connect(localhost:5432, ssl=true, timeout=30s)
// connect(db.internal:5432, ssl=true, timeout=5s)
// §3
// sum of -3 and 9 is 6
// sum of 1 and 1 is 2
// kotlin.Unit
// 144
// [positive, negative, zero]
// §4
// 0.0
// 2.5
// 20.0
// §5
// [at least 8 characters, at least one uppercase letter]
// []
// 6
// §6
// 3628800
// → factorial(3)
//   → factorial(2)
//     → factorial(1)
//     ← factorial(1) = 1
//   ← factorial(2) = 2
// ← factorial(3) = 6
// 500000500000
