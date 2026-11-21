/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 13 — Higher-order functions and `inline`
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : write functions that take or return functions, build your own
 *         map/filter, understand what `inline` does (and noinline/crossinline),
 *         and master `return` vs `return@label` vs 🆕 non-local break/continue.
 *  Needs: lesson 12 (lambdas)
 *  Time : ~40 min
 *  Next : lesson 14 — scope functions
 *  Exercises: exercises/ex13/Ex13.kt
 */
package lesson13_higher_order_and_inline

fun main() {
    section1TakingFunctions()
    section2BuildYourOwnMapAndFilter()
    section3ReturningFunctions()
    section4Inline()
    section5NoinlineAndCrossinline()
    section6ReturnsInLambdas()
    section7NonLocalBreakContinue()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. A higher-order function takes (or returns) a function
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A higher-order function is a robot arm that holds a TOOL slot. You decide
//    which tool to click in — a brush, a hammer, a spoon — and the same arm
//    does different jobs. The tool is the function you pass in.

fun add(a: Int, b: Int): Int = a + b

fun calculate(a: Int, b: Int, operation: (Int, Int) -> Int): Int = operation(a, b)

fun section1TakingFunctions() {
    println("§1")
    println(calculate(2, 3, ::add))                 // pass a named function
    println(calculate(2, 3) { a, b -> a * b })      // pass a lambda
    println(calculate(2, 3, Math::floorMod))        // pass a Java method
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Build your own `map` and `filter`
// ───────────────────────────────────────────────────────────────────────────
// The standard library's collection functions are just higher-order functions.
// Writing them yourself removes the magic. (Generics `<T, R>`: lesson 23.)

fun <T> List<T>.myFilter(keep: (T) -> Boolean): List<T> {
    val result = mutableListOf<T>()
    for (item in this) if (keep(item)) result += item
    return result
}

fun <T, R> List<T>.myMap(transform: (T) -> R): List<R> {
    val result = mutableListOf<R>()
    for (item in this) result += transform(item)
    return result
}

fun section2BuildYourOwnMapAndFilter() {
    println("§2")
    val prices = listOf(5, 120, 42, 999)
    println(prices.myFilter { it < 100 }.myMap { "$$it" })
    println(prices.filter { it < 100 }.map { "$$it" })      // the real ones: same result
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Returning functions
// ───────────────────────────────────────────────────────────────────────────
// A function can MAKE and return another function — a "function factory".

fun multiplier(factor: Int): (Int) -> Int = { it * factor }

fun <A, B, C> compose(f: (A) -> B, g: (B) -> C): (A) -> C = { a -> g(f(a)) }

fun section3ReturningFunctions() {
    println("§3")
    val double = multiplier(2)
    val triple = multiplier(3)
    println("${double(10)} ${triple(10)}")
    val lengthThenDouble = compose(String::length, double)
    println(lengthThenDouble("kotlin"))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. `inline` — paste the code instead of calling it
// ───────────────────────────────────────────────────────────────────────────
// Every lambda you pass is normally turned into an OBJECT at runtime (an
// allocation), and calling it is an extra jump. For tiny helpers called in hot
// loops that overhead adds up.
//
// 🧒 Like I'm 5:
//    Normal call: you phone a friend and read them the recipe card each time.
//    Inline: you photocopy the recipe straight into your own notebook, so
//    there's no phone call at all. Faster — but your notebook gets thicker.
//
// `inline fun` tells the compiler: copy this function's body AND the lambda's
// body into every call site. Bonus powers: non-local `return` (§6) and
// `reified` type parameters (lesson 27). Most stdlib collection functions are inline.
//
// Rules of thumb: inline small functions that take lambdas. Don't inline big
// functions (code bloat) or functions without lambda parameters (no benefit,
// the compiler warns).

inline fun measure(label: String, block: () -> Unit) {
    println("start $label")
    block()                                   // the lambda body is pasted here
    println("end $label")
}

fun section4Inline() {
    println("§4")
    measure("work") { println("  doing the work") }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. `noinline` and `crossinline`
// ───────────────────────────────────────────────────────────────────────────
// Inside an inline function a lambda parameter has no object, so you can't
// STORE it or pass it to a non-inline function. Two escape hatches:
//   noinline    → "don't inline THIS lambda; keep it as an object" (so you can store it)
//   crossinline → "inline it, but it will run inside another lambda/object,
//                  so the caller's lambda must NOT use non-local return"

val savedCallbacks = mutableListOf<() -> Unit>()

inline fun register(now: () -> Unit, noinline later: () -> Unit) {
    now()
    savedCallbacks += later                   // storing needs an object → noinline
}

inline fun runInWrapper(crossinline action: () -> Unit): Runnable =
    Runnable { action() }                     // action runs inside another object → crossinline

fun section5NoinlineAndCrossinline() {
    println("§5")
    register(now = { println("runs immediately") }, later = { println("runs later") })
    savedCallbacks.forEach { it() }
    runInWrapper { println("ran inside a Runnable") }.run()
}

// ───────────────────────────────────────────────────────────────────────────
// §6. `return` inside lambdas: non-local vs labelled
// ───────────────────────────────────────────────────────────────────────────
// In a lambda passed to an INLINE function (like forEach), a bare `return`
// returns from the ENCLOSING FUNCTION (non-local return) — it leaves the whole thing.
// `return@forEach` returns only from the current lambda call — like `continue`.
//
// 🧒 Like I'm 5:
//    You're checking a basket of eggs one by one.
//    `return@forEach` = "this egg is cracked, skip it, look at the next one".
//    `return`         = "this egg is cracked, I'm going home!" (stop everything).

fun printOddNumbersSkipping() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it % 2 == 0) return@forEach          // skip evens, keep going
        print("$it ")
    }
    println("<- loop finished")
}

fun printOddNumbersStopping() {
    listOf(1, 2, 3, 4, 5).forEach {
        if (it % 2 == 0) return                  // leaves printOddNumbersStopping entirely
        print("$it ")
    }
    println("never printed")
}

fun section6ReturnsInLambdas() {
    println("§6")
    printOddNumbersSkipping()
    printOddNumbersStopping()
    println("<- function returned early")
    val firstBig = listOf(3, 30, 300).map label@{ if (it > 100) return@label "big" else "$it" }
    println(firstBig)                            // custom label names work too
}

// ───────────────────────────────────────────────────────────────────────────
// §7. 🆕 Non-local `break` and `continue` (stable since Kotlin 2.2)
// ───────────────────────────────────────────────────────────────────────────
// Inside a lambda passed to an inline function that sits in a LOOP, you can
// now use `break` and `continue` for that loop directly.

fun section7NonLocalBreakContinue() {
    println("§7")
    val orders = listOf("ok-1", "", "ok-2", "STOP", "ok-3")
    for (order in orders) {
        order.let {
            if (it.isEmpty()) continue           // 🆕 continue the for-loop from inside `let`
            if (it == "STOP") break              // 🆕 break the for-loop from inside `let`
            print("$it ")
        }
    }
    println()
}

// ⚠️ Gotcha: a bare `return` in a lambda only compiles if the function taking
// the lambda is `inline`. With a non-inline function you must use `return@label`.

// 🏗️ In the real world: `transaction { … }`, `measureTimeMillis { … }`,
// `synchronized(lock) { … }`, `use { … }` — "do setup, run your block, do
// cleanup" helpers are inline higher-order functions. You'll write your own.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Higher-order functions take/return functions; map/filter are just that.
//  • `inline` pastes the body + lambda at the call site: no lambda objects.
//  • `noinline` to store a lambda, `crossinline` when it runs in another context.
//  • In inline lambdas: `return` exits the outer function, `return@label` the lambda.
//  • 🆕 `break` / `continue` work from inline lambdas inside loops.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 5
// 6
// 2
// §2
// [$5, $42]
// [$5, $42]
// §3
// 20 30
// 12
// §4
// start work
//   doing the work
// end work
// §5
// runs immediately
// runs later
// ran inside a Runnable
// §6
// 1 3 5 <- loop finished
// 1 <- function returned early
// [3, 30, big]
// §7
// ok-1 ok-2
