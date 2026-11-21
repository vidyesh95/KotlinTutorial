/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 26 — Generics, part 4: use-site variance and star projections
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : use `out`/`in` at the CALL SITE for classes that are invariant
 *         (MutableList, Array), and handle "a list of I-don't-know-what" with `*`.
 *  Needs: lesson 25 (variance) — this lesson reuses its fruit basket story
 *  Time : ~30 min
 *  Next : lesson 27 — type erasure and `reified`
 *  Exercises: exercises/ex26/Ex26.kt
 */
package lesson26_use_site_variance_and_star

fun main() {
    section1ProjectionsAtTheCallSite()
    section2GenericCopy()
    section3WhatAProjectionAllows()
    section4StarProjection()
    section5StarVersusAnyNullable()
}

open class Fruit(val name: String) {
    override fun toString() = name
}

class Apple(variety: String) : Fruit(variety)
class Banana : Fruit("banana")

// ───────────────────────────────────────────────────────────────────────────
// §1. Projections at the call site
// ───────────────────────────────────────────────────────────────────────────
// Lesson 25: `out`/`in` written on the CLASS (`interface Producer<out T>`) is
// *declaration-site variance*. But MutableList<E> must stay invariant — it
// reads AND writes. So this does not compile:
//
//     fun copyFruits(from: MutableList<Fruit>, to: MutableList<Fruit>)
//     copyFruits(appleList, fruitList)   // ❌ MutableList<Apple> ≠ MutableList<Fruit>
//
// Yet copying only READS `from` and only WRITES `to`. We can say so right in
// the parameter types. This is *use-site variance* (a "type projection"):
//
// 🧒 Like I'm 5:
//    The basket is still a normal basket. But for this one job you put on a
//    special glove: the `out` glove can only TAKE things out of a basket,
//    the `in` mitten can only DROP things in. With the out-glove on, lending
//    your apple basket is safe again — nobody can sneak a banana in.

fun copyInto(from: MutableList<out Fruit>, to: MutableList<in Fruit>) {
    for (fruit in from) to.add(fruit)
    // from.add(Banana())   // ❌ the `out` glove: adding is forbidden on MutableList<out Fruit>
}

fun section1ProjectionsAtTheCallSite() {
    println("§1")
    val apples: MutableList<Apple> = mutableListOf(Apple("gala"), Apple("fuji"))
    val anything: MutableList<Any> = mutableListOf("note")
    copyInto(apples, anything)    // ✅ out Fruit accepts MutableList<Apple>; in Fruit accepts MutableList<Any>
    println(anything)
}

// ───────────────────────────────────────────────────────────────────────────
// §2. The generic version you'll see everywhere
// ───────────────────────────────────────────────────────────────────────────
// "Copy T's from a source into any destination that can hold T's":

fun <T> copyAll(from: List<T>, to: MutableList<in T>) {
    to.addAll(from)
}

fun section2GenericCopy() {
    println("§2")
    val numbers: MutableList<Number> = mutableListOf(0.5)
    copyAll(listOf(1, 2), numbers)         // T = Int; MutableList<Number> can hold Ints
    println(numbers)

    // The standard library does the same:
    //   fun <T, C : MutableCollection<in T>> Iterable<T>.toCollection(destination: C): C
    println(listOf(1, 2).toCollection(mutableListOf<Any>("x")))
}

// ───────────────────────────────────────────────────────────────────────────
// §3. What a projection does to the members
// ───────────────────────────────────────────────────────────────────────────
//   MutableList<out Fruit>:  get() returns Fruit ✅     add(x) — forbidden ❌
//   MutableList<in Fruit>:   add(Fruit) ✅               get() returns only Any? (could be a list of Any)
//
// The compiler literally rewrites the signatures. With `out`, a parameter of
// type E becomes `Nothing` (no value can be passed). With `in`, a return type
// E becomes `Any?` (the most it can promise).

fun peekFirst(list: MutableList<out Fruit>): Fruit = list.first()
fun readBack(list: MutableList<in Fruit>): Any? = list.first()

fun section3WhatAProjectionAllows() {
    println("§3")
    println(peekFirst(mutableListOf(Apple("gala"))))
    println(readBack(mutableListOf<Any>("note")))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Star projection `*`: "some type, I don't know which"
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A mystery box. You know it's a box OF SOMETHING. You may look inside and
//    hold what you find (it's "some thing"), but you may not put anything in —
//    you don't know what's allowed in there.
//
// `List<*>` = a list of some unknown type. Reading gives `Any?`. For mutable
// types, writing is forbidden. It's perfect when you don't care about T:
// sizes, printing, type checks.

fun describe(x: Any?): String = when (x) {
    null -> "nothing"
    is List<*> -> "list of ${x.size}, first=${x.firstOrNull()}"   // `is List<String>` is impossible (lesson 27)
    is Map<*, *> -> "map with keys ${x.keys}"
    is Set<*> -> "set of ${x.size}"
    else -> "a ${x::class.simpleName}"
}

fun section4StarProjection() {
    println("§4")
    println(describe(listOf(1, 2)))
    println(describe(mapOf("a" to 1)))
    println(describe(setOf('x')))
    println(describe(3.5))
    println(describe(null))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. `*` is NOT the same as `Any?`
// ───────────────────────────────────────────────────────────────────────────
//   MutableList<Any?> = "a list that accepts ANYTHING, including null".
//                       A MutableList<String> is NOT one (you could add an Int to it!).
//   MutableList<*>    = "a list of some type I don't know". Every MutableList IS one.

fun clearAll(list: MutableList<*>) = list.clear()   // works for ANY mutable list

fun addNull(list: MutableList<Any?>) {
    list.add(null)
}

fun section5StarVersusAnyNullable() {
    println("§5")
    val words = mutableListOf("a", "b")
    // addNull(words)            // ❌ MutableList<String> is not MutableList<Any?>
    clearAll(words)              // ✅
    println(words)

    val bag = mutableListOf<Any?>("x")
    addNull(bag)
    println(bag)
}

// Which one when?
// ┌──────────────────────────────┬───────────────────────────────────────────┐
// │ You own the class and T is   │ declaration-site: `class Source<out T>`   │
// │ only produced / consumed     │ (lesson 25) — callers never think about it │
// ├──────────────────────────────┼───────────────────────────────────────────┤
// │ Class is invariant, but THIS │ use-site: `fun f(src: MutableList<out T>)`│
// │ function only reads/writes   │                                           │
// ├──────────────────────────────┼───────────────────────────────────────────┤
// │ You don't care about T       │ star: `List<*>`, `Map<*, *>`, `KClass<*>` │
// └──────────────────────────────┴───────────────────────────────────────────┘
//
// 🏗️ In the real world: Java's `List<? extends Fruit>` = Kotlin `MutableList<out Fruit>`,
// `List<? super Fruit>` = `MutableList<in Fruit>`, `List<?>` = `MutableList<*>`.
// You'll see these when Spring/Java APIs show up in Kotlin signatures.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Use-site variance: put `out`/`in` on a parameter's type argument.
//  • `out` projection → can read T, cannot write. `in` → can write T, reads give Any?.
//  • `*` = unknown type argument: read as Any?, never write. Not the same as Any?.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// [note, gala, fuji]
// §2
// [0.5, 1, 2]
// [x, 1, 2]
// §3
// gala
// note
// §4
// list of 2, first=1
// map with keys [a]
// set of 1
// a Double
// nothing
// §5
// []
// [x, null]
