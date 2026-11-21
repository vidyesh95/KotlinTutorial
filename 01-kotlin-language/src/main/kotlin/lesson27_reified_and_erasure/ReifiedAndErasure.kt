/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 27 — Generics, part 5: type erasure and `reified`
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : know why `x is List<String>` is impossible on the JVM, and how
 *         `inline fun <reified T>` gives you the type back at runtime.
 *  Needs: lessons 13 (inline functions), 23–26
 *  Time : ~30 min
 *  Next : lesson 28 — context parameters
 *  Exercises: exercises/ex27/Ex27.kt
 */
package lesson27_reified_and_erasure

import kotlin.reflect.KClass
import kotlin.reflect.typeOf

fun main() {
    section1Erasure()
    section2TheOldWorkaround()
    section3Reified()
    section4TypeInfoAtRuntime()
    section5RealWorldParsing()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Erasure: type arguments disappear at runtime
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    The labels on our generic boxes (lesson 23) are written in DISAPPEARING
//    INK. While the compiler builds your program it reads every label and
//    checks every put-in and take-out. Then the ink fades. When the program
//    runs, a `Box<String>` and a `Box<Int>` look exactly the same: just "Box".
//
// Precisely: the JVM has no generics in its bytecode (Java added generics
// late and kept old code working by *erasing* them). So at runtime you can
// ask "is this a List?" but not "is this a List of Strings?".

fun section1Erasure() {
    println("§1")
    val strings: List<String> = listOf("a", "b")
    val ints: List<Int> = listOf(1, 2)
    println(strings.javaClass == ints.javaClass)    // same runtime class!

    val unknown: Any = strings
    // if (unknown is List<String>) …   // ❌ Cannot check for instance of erased type 'List<String>'
    if (unknown is List<*>) println("it's a list of ${unknown.size} somethings")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. The problem, and the old workaround
// ───────────────────────────────────────────────────────────────────────────
// We want "keep only the elements of type T":
//
//     fun <T> onlyOf(items: List<Any>): List<T> = items.filter { it is T }
//     // ❌ Cannot check for instance of erased type 'T'
//
// Classic fix (Java style): pass the type as a VALUE, a Class<T> object, so
// it survives until runtime.

fun <T> filterByClass(items: List<Any>, type: Class<T>): List<T> =
    items.filter(type::isInstance).map(type::cast)

val mixed: List<Any> = listOf(1, "two", 3.0, "four", 5)

fun section2TheOldWorkaround() {
    println("§2")
    println(filterByClass(mixed, String::class.java))
    println(filterByClass(mixed, Int::class.javaObjectType))  // clumsy: must ask for the BOXED java.lang.Integer
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `reified`: keep the label in permanent ink
// ───────────────────────────────────────────────────────────────────────────
// An `inline` function's body is COPIED into every place it is called
// (lesson 13). At each call site the compiler knows the real type argument,
// so it can paste the real type in. Mark the parameter `reified` to ask for
// exactly that. Now `is T`, `as T` and `T::class` work.
//
// 🧒 Like I'm 5:
//    Instead of one shared toy box with a fading label, the factory builds
//    a fresh copy of the box for every kid who orders one, and prints that
//    kid's label on it in permanent ink.

inline fun <reified T> List<Any?>.only(): List<T> = filter { it is T }.map { it as T }

fun section3Reified() {
    println("§3")
    println(mixed.only<String>())
    println(mixed.only<Int>())
    println(mixed.only<Number>())      // Int and Double are both Numbers
    // The stdlib already ships this one: mixed.filterIsInstance<String>()
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Getting type information: `T::class` and `typeOf<T>()`
// ───────────────────────────────────────────────────────────────────────────

inline fun <reified T : Any> className(): String = T::class.simpleName ?: "?"

// typeOf<T>() returns a KType that still knows the type ARGUMENTS (List<Book>, not just List).
inline fun <reified T> typeArgumentNames(): List<String?> =
    typeOf<T>().arguments.map { (it.type?.classifier as? KClass<*>)?.simpleName }

fun section4TypeInfoAtRuntime() {
    println("§4")
    println(className<String>())
    println(className<List<Int>>())        // KClass only knows the CLASS: "List"
    println(typeArgumentNames<Map<String, Int>>())  // typeOf keeps the type ARGUMENTS too
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Real-world: "give me this text as a T"
// ───────────────────────────────────────────────────────────────────────────
// JSON libraries and HTTP clients need to know the TARGET type at runtime to
// build the right object. A miniature version:

val parsers: Map<KClass<*>, (String) -> Any> = mapOf(
    Int::class to String::toInt,
    Boolean::class to String::toBooleanStrict,
)

inline fun <reified T : Any> parse(raw: String): T {
    val parser = parsers[T::class] ?: error("No parser for ${T::class.simpleName}")
    return parser(raw) as T
}

fun section5RealWorldParsing() {
    println("§5")
    println(parse<Int>("42") + 1)          // the result is a real Int — we can do math
    println(parse<Boolean>("true"))
    println(runCatching { parse<Double>("1.0") }.exceptionOrNull()?.message)
}
// 🏗️ In the real world (Spring, later modules):
//    restClient.get().uri("/books/1").retrieve().body<Book>()        ← reified T
//    restClient.get().uri("/books").retrieve().body<List<Book>>()    ← typeOf keeps List<Book>
//    In Java you'd write `new ParameterizedTypeReference<List<Book>>() {}` — reified hides that.

// ⚠️ Gotchas:
//  • `typeOf<T>().toString()` prints pretty Kotlin names only with the full
//    `kotlin-reflect` library on the classpath; without it you get Java names plus
//    "(Kotlin reflection is not available)". `simpleName` and `arguments` work either way.
//  • `reified` only works on `inline` functions — not on classes, not on normal functions.
//  • Inline functions are copied at each call: keep them small.
//  • Java code cannot call reified functions (Java has no way to supply T).
//  • An unchecked cast `x as List<String>` compiles with a WARNING and checks
//    only "is it a List" at runtime — wrong element types blow up later, far away.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • JVM erases type arguments: at runtime List<String> is just List.
//  • Work-around 1: pass a Class<T>/KClass<T> value explicitly.
//  • Work-around 2 (idiomatic): `inline fun <reified T>` → `is T`, `as T`, `T::class`.
//  • `typeOf<T>()` keeps nested arguments like List<Book>.
//  This finishes generics. You now know more about them than many working devs. 🎉
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// true
// it's a list of 2 somethings
// §2
// [two, four]
// [1, 5]
// §3
// [two, four]
// [1, 5]
// [1, 3.0, 5]
// §4
// String
// List
// [String, Int]
// §5
// 43
// true
// No parser for Double
