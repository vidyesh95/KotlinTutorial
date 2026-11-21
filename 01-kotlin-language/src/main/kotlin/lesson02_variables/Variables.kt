/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 02 — Variables: `val`, `var`, types and scope
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : store values in variables, know when to use `val` vs `var`,
 *         let Kotlin infer types (or write them yourself), use `const val`,
 *         and understand where a variable "lives" (scope).
 *  Needs: lesson 01
 *  Time : ~20 min
 *  Next : lesson 03 — numbers and basic types
 *  Exercises: exercises/ex02/Ex02.kt
 */
package lesson02_variables

fun main() {
    section1ValAndVar()
    section2TypeInference()
    section3ConstVal()
    section4Naming()
    section5Scope()
    section6ImmutabilityMindset()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. `val` and `var`
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A variable is a labelled jar. The label is the NAME, what's inside is the
//    VALUE.
//    • `val` jar has its lid GLUED shut after you fill it once (read-only).
//    • `var` jar has a normal lid: you can empty it and put something new in.
//
// Precisely:
//    val name = value   → read-only reference, assigned exactly once ("value")
//    var name = value   → mutable reference, can be reassigned ("variable")

fun section1ValAndVar() {
    println("§1")
    val birthYear = 1815          // will never change
    var score = 7                 // will change
    score = score + 1             // ✅ reassign a var (now 8)
    score += 2                    // shorthand for score = score + 2 (now 10)
    // birthYear = 1816           // ❌ compile error: 'val' cannot be reassigned
    println("birthYear=$birthYear score=$score")

    // A val can be declared first and assigned later — but only ONCE:
    val greeting: String
    val hour = 9
    greeting = if (hour < 12) "Good morning" else "Good afternoon"
    println(greeting)
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Type inference and explicit types
// ───────────────────────────────────────────────────────────────────────────
// Every variable has a TYPE (Int, String, Double, ...). Usually Kotlin works it
// out from the value on the right — this is TYPE INFERENCE. You can also write
// the type after a colon:  `val count: Int = 3`.
//
// Once chosen, a variable's type never changes (Kotlin is STATICALLY typed).
//
// Write the type explicitly when:
//   • you declare without a value (`val greeting: String` above),
//   • you want a different type than the default (`val big: Long = 3`),
//   • it makes public API clearer to readers.

fun section2TypeInference() {
    println("§2")
    val count = 3                 // inferred Int
    val price = 9.99              // inferred Double
    val title = "Kotlin"          // inferred String
    val isFun = true              // inferred Boolean
    val big: Long = 3             // explicit: 3 would be Int by default
    val ratio: Double = 1.0
    println("$count $price $title $isFun $big $ratio")

    var answer = 42
    // answer = "forty-two"       // ❌ Type mismatch: the jar is labelled Int forever
    answer -= 1
    println(answer)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `const val` — compile-time constants
// ───────────────────────────────────────────────────────────────────────────
// `const val` is a `val` whose value is known while COMPILING (a literal number,
// String, Boolean...). The compiler pastes the value wherever it's used.
// Rules: top level, inside an `object`, or in a `companion object` only; and
// only primitive types or String. Naming convention: SCREAMING_SNAKE_CASE.
//
// 🏗️ In the real world: annotation arguments in Spring must be constants,
//    e.g. `@RequestMapping(API_PREFIX)` needs `const val API_PREFIX = "/api"`.

const val MAX_LOGIN_ATTEMPTS = 5
const val API_PREFIX = "/api/v1"

fun section3ConstVal() {
    println("§3")
    println("Max attempts: $MAX_LOGIN_ATTEMPTS, prefix: $API_PREFIX")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Naming rules and conventions
// ───────────────────────────────────────────────────────────────────────────
// Rules (the compiler enforces): letters, digits, `_`; can't start with a digit;
// can't be a hard keyword like `val`, `fun`, `class` (unless wrapped in
// backticks: `class` — avoid that except in tests).
//
// Conventions (humans enforce):
//   • variables & functions: lowerCamelCase       → userName, totalPrice
//   • constants:             SCREAMING_SNAKE_CASE → MAX_SIZE
//   • classes:               UpperCamelCase       → UserAccount
//   • be descriptive: `elapsedSeconds` beats `t`.

fun section4Naming() {
    println("§4")
    val userName = "grace"
    val itemsInCart = 3
    println("$userName has $itemsInCart items")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Scope — where a variable is visible
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Every pair of `{ }` is a room. A variable made in a room can be used in
//    that room and in rooms INSIDE it — but not outside. When you leave the
//    room, its variables are gone.
//
// A variable in an inner block may have the same name as one outside: it
// SHADOWS (hides) the outer one. Legal, but confusing — the compiler warns.

val topLevelCounter = 100         // top-level: visible in this whole file (and package)

fun section5Scope() {
    println("§5")
    val outer = "outer"
    if (outer.isNotEmpty()) {
        val inner = "inner"       // only exists inside this if-block
        println("$outer sees $inner")
    }
    // println(inner)             // ❌ Unresolved reference: inner is out of scope here
    println("top level: $topLevelCounter")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. The immutability mindset
// ───────────────────────────────────────────────────────────────────────────
// Rule of thumb: ALWAYS start with `val`. Switch to `var` only when the
// compiler tells you that you need to reassign.
//
// Why? Fewer things change → fewer things can go wrong. A `val` can be read
// by many threads at once safely, and you never wonder "who changed this?".
//
// ⚠️ Gotcha: `val` means the REFERENCE can't change, not that the object is
//    frozen. A `val list = mutableListOf(1)` can still `list.add(2)`.
//    The jar's lid is glued, but the toy inside can still be painted.

fun section6ImmutabilityMindset() {
    println("§6")
    val numbers = mutableListOf(1, 2)
    numbers.add(3)                // ✅ allowed: we change the list, not the reference
    // numbers = mutableListOf()  // ❌ not allowed: that would change the reference
    println(numbers)

    // Prefer computing a NEW value over mutating:
    val prices = listOf(10, 20, 30)
    val total = prices.sum()      // no `var total = 0` + loop needed
    println("total=$total")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `val` = read-only (assign once). `var` = can be reassigned. Prefer `val`.
//  • Types are inferred from the value; write `: Type` when needed or clearer.
//  • `const val` = compile-time constant, top level/object only, primitive or String.
//  • Variables live inside the `{ }` they're declared in (scope).
//  • `val` freezes the reference, not the object it points to.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// birthYear=1815 score=10
// Good morning
// §2
// 3 9.99 Kotlin true 3 1.0
// 41
// §3
// Max attempts: 5, prefix: /api/v1
// §4
// grace has 3 items
// §5
// outer sees inner
// top level: 100
// §6
// [1, 2, 3]
// total=60
