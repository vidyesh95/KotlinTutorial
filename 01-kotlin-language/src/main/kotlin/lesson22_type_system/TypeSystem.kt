/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 22 — The type system: Any, Unit, Nothing, checks, casts, equality
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : see the whole Kotlin type tree, check and cast types safely,
 *         understand when smart casts work, name types with `typealias`,
 *         and master == vs === and the equals/hashCode contract.
 *  Needs: lessons 06 (null safety), 15–21
 *  Time : ~35 min
 *  Next : lesson 23 — generics, part 1
 *  Exercises: exercises/ex22/Ex22.kt
 */
package lesson22_type_system

fun main() {
    section1TheTypeTree()
    section2UnitAndNothing()
    section3TypeChecksAndCasts()
    section4WhenSmartCastsFail()
    section5TypeAliases()
    section6Equality()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The type tree
// ───────────────────────────────────────────────────────────────────────────
//
//                         Any?                 ← top: EVERY value, including null
//                       /      \
//                    Any        Nothing?       ← (Nothing? has exactly one value: null)
//         ┌─────────┼──────────┐   |
//      String      Int   YourClass…  |
//         |         |       |        |
//      String?    Int?  YourClass?  ← every T? = "T or null"
//                       \     |     /
//                         Nothing              ← bottom: NO value at all
//
// 🧒 Like I'm 5:
//    `Any` is the word "thing" — a cat is a thing, a number is a thing.
//    `Any?` is "a thing, or an empty hand".
//    `Nothing` is a box that can never be opened because it is never filled:
//    code that returns Nothing never comes back (it throws, or loops forever).
//
// Facts:
//   • `Any` is the parent of all non-null types; it has equals, hashCode, toString.
//   • `T` is a subtype of `T?`  — a String fits where a String? is expected.
//   • `Nothing` is a subtype of EVERY type — that's why `throw` fits anywhere.

fun describe(value: Any?): String = when (value) {
    null -> "null"
    is String -> "String of length ${value.length}"
    is Int -> "Int, doubled ${value * 2}"
    is List<*> -> "List with ${value.size} elements"
    else -> "something else: ${value::class.simpleName}"
}

fun section1TheTypeTree() {
    println("§1")
    val things: List<Any?> = listOf("hi", 21, listOf(1, 2, 3), 3.5, null)
    things.forEach { println(describe(it)) }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Unit and Nothing
// ───────────────────────────────────────────────────────────────────────────
// `Unit` = "returns, but with nothing interesting". A function without a return
// type returns Unit. Unlike Java's `void`, Unit is a real object (`kotlin.Unit`),
// so it works as a generic argument: `() -> Unit`, `Result<Unit>`.
//
// `Nothing` = "never returns normally". `throw`, `TODO()`, `error()` all have
// type Nothing. Because Nothing fits every type, you can use them anywhere an
// expression is expected — e.g. on the right of `?:`.

fun logLine(text: String) {                       // return type Unit, implicitly
    println("log: $text")
}

fun fail(message: String): Nothing = throw IllegalStateException(message)

fun portFrom(config: Map<String, String>): Int =
    config["port"]?.toIntOrNull() ?: fail("port missing or invalid")   // Nothing fits where Int is expected

fun section2UnitAndNothing() {
    println("§2")
    val result: Unit = logLine("hello")
    println("logLine returned $result")
    println(portFrom(mapOf("port" to "8080")))
    println(runCatching { portFrom(emptyMap()) }.exceptionOrNull()?.message)
    println(runCatching { TODO("write me") }.exceptionOrNull()?.message)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Type checks and casts: is, !is, as, as?
// ───────────────────────────────────────────────────────────────────────────
//   x is T      → true if x is a T (or a subtype). After a successful check
//                 the compiler SMART-CASTS x to T — no manual cast needed.
//   x !is T     → the opposite
//   x as T      → "trust me, it's a T". Wrong? ClassCastException at runtime.
//   x as? T     → safe cast: T if it fits, otherwise null. Pair it with ?:
//
// ⚠️ Gotcha: prefer `is` + smart cast or `as?`. A bare `as` is a promise the
//    compiler can't check — a crash waiting to happen.

fun stringLength(obj: Any): Int? {
    if (obj is String && obj.isNotEmpty()) {      // smart cast already works in the right side of &&
        return obj.length
    }
    return null
}

fun section3TypeChecksAndCasts() {
    println("§3")
    for (obj in listOf<Any>("Incomprehensibility", 1000, "")) {
        println("$obj -> ${stringLength(obj) ?: "not a non-empty string"}")
    }
    val value: Any = 42
    println(value as? String)                     // null, no crash
    println((value as? Int)?.plus(1))
    println(runCatching { value as String }.exceptionOrNull()?.let { it::class.simpleName })
    if (value !is String) println("value is not a String")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. When smart casts DON'T work
// ───────────────────────────────────────────────────────────────────────────
// The compiler only smart-casts when it can PROVE the value can't change
// between the check and the use:
//   ✅ local `val`, and local `var` not captured & modified in a lambda
//   ✅ `val` property without a custom getter, declared in the same module
//   ❌ `var` property — another thread/function could change it in between
//   ❌ `open` property or one with a custom getter — could return something else
// Fix: copy it into a local `val` first, or use `?.let { }`.

class Profile {
    var nickname: String? = "ada"
}

fun section4WhenSmartCastsFail() {
    println("§4")
    val profile = Profile()
    // if (profile.nickname != null) println(profile.nickname.length)
    //     ❌ Smart cast to 'String' is impossible, because 'nickname' is a mutable property
    //        that could be mutated concurrently.
    val nick = profile.nickname                   // fix 1: local copy
    if (nick != null) println(nick.length)
    profile.nickname?.let { println(it.uppercase()) }   // fix 2: let
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Type aliases — a nickname for a type
// ───────────────────────────────────────────────────────────────────────────
// `typealias Name = ExistingType` creates NO new type — just a shorter name.
// Great for long function types and generic types.
//
// ⚠️ Gotcha: since it's the same type, aliases don't stop mix-ups.
//    `typealias UserId = Long` and `typealias OrderId = Long` are freely
//    interchangeable. For real safety use a value class (lesson 17).

typealias Headers = Map<String, List<String>>
typealias Validator = (String) -> Boolean
typealias UserIdAlias = Long

fun firstHeader(headers: Headers, name: String): String? = headers[name]?.firstOrNull()

fun section5TypeAliases() {
    println("§5")
    val headers: Headers = mapOf("Accept" to listOf("application/json", "text/plain"))
    println(firstHeader(headers, "Accept"))

    val notBlank: Validator = { it.isNotBlank() }
    println(notBlank("  "))

    val id: UserIdAlias = 7L
    val plainLong: Long = id                      // no conversion needed: same type
    println(plainLong)
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Equality: == vs ===, and the equals/hashCode contract
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5: two identical teddy bears from the same shop.
//    `==`  asks "do they LOOK the same?"     (structural: calls equals())
//    `===` asks "is it the SAME bear?"       (referential: same object in memory)
//
// `a == b` is null-safe: it means `a?.equals(b) ?: (b === null)`.
// Plain classes inherit equals from Any, which IS identity. Data classes
// generate content-based equals.
//
// The contract (break it and HashSet/HashMap misbehave):
//   • if a == b then a.hashCode() == b.hashCode()
//   • equals is reflexive, symmetric, transitive, and consistent
//   → whenever you override equals, override hashCode too (or use a data class).

class Bear(val color: String)

class BadKey(val id: Int) {                       // equals WITHOUT hashCode: broken!
    override fun equals(other: Any?) = other is BadKey && other.id == id
    override fun toString() = "BadKey($id)"
}

class GoodKey(val id: Int) {
    override fun equals(other: Any?) = other is GoodKey && other.id == id
    override fun hashCode() = id.hashCode()
}

fun section6Equality() {
    println("§6")
    val a = Bear("brown")
    val b = Bear("brown")
    val sameAsA = a
    println("a == b: ${a == b}, a === sameAsA: ${a === sameAsA}")   // Bear isn't a data class → identity

    val s1 = "kotlin"
    val s2 = "KOTLIN".lowercase()                 // built at runtime → a different String object
    println("s1 == s2: ${s1 == s2}, s1 === s2: ${s1 === s2}")

    println("BadKey equal? ${BadKey(1) == BadKey(1)}, set size: ${hashSetOf(BadKey(1), BadKey(1)).size}")
    println("GoodKey equal? ${GoodKey(1) == GoodKey(1)}, set size: ${hashSetOf(GoodKey(1), GoodKey(1)).size}")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Any? is the top type, Any the top of non-null types, Nothing the bottom.
//  • Unit = "returned, nothing useful"; Nothing = "never returns" (throw, TODO, error).
//  • `is`/`!is` check and smart-cast; `as?` casts safely; bare `as` can crash.
//  • Smart casts fail on mutable/open/custom-getter properties → copy to a local val.
//  • `typealias` is just a nickname — no extra safety (use value classes for that).
//  • `==` compares content via equals, `===` compares identity.
//  • Override equals ⇒ override hashCode. Data classes do both for you.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// String of length 2
// Int, doubled 42
// List with 3 elements
// something else: Double
// null
// §2
// log: hello
// logLine returned kotlin.Unit
// 8080
// port missing or invalid
// An operation is not implemented: write me
// §3
// Incomprehensibility -> 19
// 1000 -> not a non-empty string
//  -> not a non-empty string
// null
// 43
// ClassCastException
// value is not a String
// §4
// 3
// ADA
// §5
// application/json
// false
// 7
// §6
// a == b: false, a === sameAsA: true
// s1 == s2: true, s1 === s2: false
// BadKey equal? true, set size: 2
// GoodKey equal? true, set size: 1
