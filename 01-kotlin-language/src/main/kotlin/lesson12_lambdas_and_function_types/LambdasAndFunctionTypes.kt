/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 12 — Lambdas and function types
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : treat functions as VALUES: write lambdas, read function types like
 *         `(Int) -> String`, use `it`, trailing lambdas, function references
 *         (`::name`), closures, anonymous functions and `fun interface`.
 *  Needs: lesson 11 (functions)
 *  Time : ~35 min
 *  Next : lesson 13 — higher-order functions and inline
 *  Exercises: exercises/ex12/Ex12.kt
 */
package lesson12_lambdas_and_function_types

fun main() {
    section1LambdaSyntax()
    section2FunctionTypes()
    section3ItAndTrailingLambdas()
    section4FunctionReferences()
    section5Closures()
    section6AnonymousFunctions()
    section7FunInterfaces()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. A lambda is a function without a name
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A normal function is a recipe printed in a cookbook with a title.
//    A lambda is a recipe written on a loose index card. No title — but you can
//    hand the card to someone else ("here, bake this"), keep it in your pocket
//    (store it in a variable), or put it in a box with other cards (a list).
//
//   { a: Int, b: Int -> a + b }
//     ─────────────    ─────
//     parameters   ->  body (last expression = the result, no `return`)

fun section1LambdaSyntax() {
    println("§1")
    val sum = { a: Int, b: Int -> a + b }          // stored in a variable
    println(sum(2, 3))                              // called like a function
    println(sum.invoke(2, 3))                       // same thing, spelled out

    val greet = { println("hello from a lambda") } // no parameters
    greet()

    val describe = { n: Int ->
        val parity = if (n % 2 == 0) "even" else "odd"
        "$n is $parity"                             // last line = result
    }
    println(describe(7))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Function types
// ───────────────────────────────────────────────────────────────────────────
// A lambda's TYPE describes its shape:
//     (Int, Int) -> Int     takes two Ints, returns an Int
//     (String) -> Unit      takes a String, returns nothing useful
//     () -> Boolean         takes nothing, returns a Boolean
//     ((Int) -> Int)?       a NULLABLE function (may be absent) — note the parentheses
// When the variable's type is declared, the lambda's parameter types can be inferred.

fun section2FunctionTypes() {
    println("§2")
    val multiply: (Int, Int) -> Int = { a, b -> a * b }       // types come from the declaration
    val isEven: (Int) -> Boolean = { n -> n % 2 == 0 }
    val shout: (String) -> Unit = { text -> println(text.uppercase() + "!") }

    println(multiply(6, 7))
    println(isEven(4))
    shout("lambdas are values")

    val operations: Map<String, (Int, Int) -> Int> = mapOf(
        "+" to { a, b -> a + b },
        "max" to { a, b -> maxOf(a, b) },
    )
    for ((name, op) in operations) println("$name -> ${op(3, 9)}")

    val maybeCallback: ((Int) -> Unit)? = null
    maybeCallback?.invoke(4)                                   // safe call: does nothing if null
    println("callback present? ${maybeCallback != null}")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `it` and trailing lambdas
// ───────────────────────────────────────────────────────────────────────────
// • Lambda with exactly ONE parameter? You may skip naming it: it's called `it`.
// • If a function's LAST parameter is a function, the lambda can go OUTSIDE
//   the parentheses ("trailing lambda"). If it's the only argument, drop `()`.
// This is why Kotlin code reads like `list.filter { it > 0 }`.

fun twice(action: (Int) -> Unit) {
    action(1)
    action(2)
}

fun section3ItAndTrailingLambdas() {
    println("§3")
    val numbers = listOf(5, -2, 8, -1)
    println(numbers.filter({ n -> n > 0 }))         // explicit, parenthesised
    println(numbers.filter { n -> n > 0 })          // trailing lambda
    println(numbers.filter { it > 0 })              // + implicit `it`
    twice { println("round $it") }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Function references `::`
// ───────────────────────────────────────────────────────────────────────────
// Already have a named function that does the job? Pass it by reference
// instead of wrapping it in a lambda.
//   ::isPrime         top-level function
//   String::length    member of a type (receiver becomes the first parameter)
//   "abc"::plus       member bound to a specific object
//   ::Person          constructor

fun isPrime(n: Int): Boolean = n >= 2 && (2..<n).none { n % it == 0 }

data class Person(val name: String)

fun section4FunctionReferences() {
    println("§4")
    println((1..15).filter(::isPrime))
    println(listOf("kiwi", "banana").map(String::length))
    val addSuffix: (String) -> String = "id-"::plus
    println(addSuffix("42"))
    println(listOf("Ada", "Linus").map(::Person))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Closures: lambdas remember their surroundings
// ───────────────────────────────────────────────────────────────────────────
// A lambda can read AND change variables from the place it was created —
// even after that place has finished. It "closes over" them.
//
// 🧒 Like I'm 5:
//    A kid leaves home with a backpack. In it is the house key (a variable from
//    home). Wherever the kid goes, the key is still in the backpack.

fun makeCounter(): () -> Int {
    var count = 0                     // lives on after makeCounter returns…
    return { ++count }                // …because this lambda captured it
}

fun section5Closures() {
    println("§5")
    val counterA = makeCounter()
    val counterB = makeCounter()      // a separate backpack, a separate `count`
    println("${counterA()} ${counterA()} ${counterA()}")
    println("${counterB()}")

    var total = 0
    listOf(1, 2, 3).forEach { total += it }   // the lambda MODIFIES an outer var
    println("total = $total")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Anonymous functions
// ───────────────────────────────────────────────────────────────────────────
// `fun(x: Int): Int { … }` — like a lambda but written as a function: explicit
// return type, and `return` returns from IT (not from the outer function).
// Rarely needed; good to recognise.

fun section6AnonymousFunctions() {
    println("§6")
    val clamp = fun(x: Int): Int {
        if (x < 0) return 0
        if (x > 100) return 100
        return x
    }
    println(listOf(-5, 50, 500).map(clamp))
}

// ───────────────────────────────────────────────────────────────────────────
// §7. `fun interface` — SAM conversion
// ───────────────────────────────────────────────────────────────────────────
// An interface with a Single Abstract Method (SAM) marked `fun interface` can
// be created from a lambda. Gives the function type a NAME and room for docs.
// Java interfaces like Runnable / Comparator get this automatically.

fun interface Validator {
    fun validate(input: String): Boolean
}

val notBlank = Validator { it.isNotBlank() }                  // lambda → Validator
val emailLike = Validator { "@" in it && "." in it.substringAfter("@") }

fun section7FunInterfaces() {
    println("§7")
    val rules = listOf(notBlank, emailLike)
    for (input in listOf("ada@example.com", "ada", "   ")) {
        println("'$input' valid? ${rules.all { it.validate(input) }}")
    }
    val byLength = Comparator<String> { a, b -> a.length - b.length }   // Java SAM interface
    println(listOf("ccc", "a", "bb").sortedWith(byLength))
}

// ⚠️ Gotcha: `val f = { x: Int -> return x }` doesn't compile — a plain `return`
// inside a lambda is not allowed (except in inline functions, lesson 13).
// The last expression is the result; use `return@label` to exit early.

// 🏗️ In the real world: Spring's Kotlin DSLs (router { }, security { }) and
// every collection operation take lambdas. Callbacks, event handlers and
// strategies are just function types or `fun interface`s.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `{ a: Int -> a * 2 }` is a lambda; its type is `(Int) -> Int`.
//  • One parameter → `it`. Last parameter is a function → trailing lambda.
//  • `::fn`, `Type::member`, `::Class` turn named functions into values.
//  • Lambdas capture (and can modify) variables around them: closures.
//  • `fun interface` lets a lambda implement a single-method interface.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 5
// 5
// hello from a lambda
// 7 is odd
// §2
// 42
// true
// LAMBDAS ARE VALUES!
// + -> 12
// max -> 9
// callback present? false
// §3
// [5, 8]
// [5, 8]
// [5, 8]
// round 1
// round 2
// §4
// [2, 3, 5, 7, 11, 13]
// [4, 6]
// id-42
// [Person(name=Ada), Person(name=Linus)]
// §5
// 1 2 3
// 1
// total = 6
// §6
// [0, 50, 100]
// §7
// 'ada@example.com' valid? true
// 'ada' valid? false
// '   ' valid? false
// [a, bb, ccc]
