/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 24 — Generics, part 2: constraints (upper bounds)
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : restrict which types may fill a type parameter, so you can USE
 *         abilities of those types inside generic code (compare, add, …).
 *  Needs: lesson 23 (generic classes and functions)
 *  Time : ~25 min
 *  Next : lesson 25 — variance (`out` / `in`)
 *  Exercises: exercises/ex24/Ex24.kt
 */
package lesson24_generic_constraints

fun main() {
    section1WhyConstraints()
    section2UpperBound()
    section3BoundedClass()
    section4SeveralBoundsWithWhere()
    section5NonNullBound()
    section6YourOwnComparable()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Why we need constraints
// ───────────────────────────────────────────────────────────────────────────
// Inside `fun <T> f(x: T)` the compiler knows NOTHING about T. It could be an
// Int, a String, a User, even null. So it only lets you do what EVERY type can
// do: `toString()`, `equals()`, `hashCode()`.
//
//     fun <T> biggest(a: T, b: T): T = if (a > b) a else b
//     // ❌ compile error: Unresolved reference 'compareTo'
//
// `>` needs `compareTo`, and not every type has it.

fun <T> describe(x: T): String = "I can only print it: $x"

fun section1WhyConstraints() {
    println("§1")
    println(describe(42))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Upper bound: `T : Something`
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    At the theme park the roller coaster has a sign: "You must be at least
//    THIS tall to ride". Anyone who reaches the line may ride — big kids,
//    grown-ups, giants. Nobody shorter.
//
//    `T : Comparable<T>` is that sign. Any type may ride, as long as it is
//    "at least" a Comparable. Because every rider is guaranteed to be
//    comparable, inside the ride we are allowed to compare them.
//
// Precisely: an *upper bound* says "T must be this type or a subtype of it".
// In exchange, the members of the bound become usable on values of type T.

fun <T : Comparable<T>> biggest(a: T, b: T): T = if (a >= b) a else b

fun <T : Comparable<T>> largest(items: List<T>): T? {
    var best: T? = null
    for (item in items) {
        if (best == null || item > best) best = item   // `>` is allowed thanks to the bound
    }
    return best
}

fun section2UpperBound() {
    println("§2")
    println(biggest(3, 7))                 // Int is Comparable<Int>
    println(biggest("apple", "banana"))    // String is Comparable<String> (alphabetical)
    println(largest(listOf(4, 9, 2)))
    println(largest(emptyList<Int>()))
    // biggest(listOf(1), listOf(2))       // ❌ List<Int> is not Comparable — too short for this ride
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Bounds on classes
// ───────────────────────────────────────────────────────────────────────────
// Classes take bounds the same way. `Number` is the parent of Int, Long,
// Double, … and offers `toDouble()`, so a `T : Number` can be averaged.

class Stats<T : Number>(private val values: List<T>) {
    fun average(): Double =
        if (values.isEmpty()) 0.0 else values.sumOf { it.toDouble() } / values.size
}

fun section3BoundedClass() {
    println("§3")
    println(Stats(listOf(1, 2, 3, 4)).average())   // Stats<Int>
    println(Stats(listOf(1.5, 2.5)).average())     // Stats<Double>
    // Stats(listOf("a", "b"))                     // ❌ String is not a Number
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Several bounds at once: `where`
// ───────────────────────────────────────────────────────────────────────────
// Need T to be TWO things? Use a `where` clause after the signature.
// Here T must have a length (CharSequence) AND be sortable (Comparable<T>).
// `String` is both, so it fits.

fun <T> sortedLongWords(items: List<T>): List<T>
        where T : CharSequence,
              T : Comparable<T> =
    items.filter { it.length > 2 }.sorted()

fun section4SeveralBoundsWithWhere() {
    println("§4")
    println(sortedLongWords(listOf("kiwi", "fig", "banana", "apple", "ok")))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. The hidden default bound, and `T : Any`
// ───────────────────────────────────────────────────────────────────────────
// Writing `<T>` really means `<T : Any?>` — nullable types are allowed.
// That's why in lesson 23 `Box<String?>` worked.
//
// When your function must never hand back null, bound T by `Any`:
// now `T` can only be a non-null type, and `T?` adds the null back explicitly.

fun <T : Any> requireFound(value: T?, what: String): T =
    value ?: throw NoSuchElementException("$what not found")

fun section5NonNullBound() {
    println("§5")
    val scores = mapOf("a" to 1, "b" to 2)
    println(requireFound(scores["a"], "key a"))      // map lookups return Int? → we get a safe Int
    val error = runCatching { requireFound(scores["z"], "key z") }.exceptionOrNull()
    println(error?.message)
}
// 🏗️ In the real world: Spring Data repositories in Kotlin are often declared
// with `T : Any`, e.g. `fun <T : Any> findById(id: Long, type: KClass<T>): T?` —
// the entity type itself is never nullable; "not found" is expressed by `T?`.

// ───────────────────────────────────────────────────────────────────────────
// §6. Make YOUR type fit a bound
// ───────────────────────────────────────────────────────────────────────────
// Implement `Comparable<Version>` and every generic function bounded by
// `Comparable<T>` — ours above and the standard library's `sorted()`,
// `maxOrNull()`, `coerceIn()` … — suddenly works with Version.

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {
    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    override fun toString() = "$major.$minor.$patch"
}

fun section6YourOwnComparable() {
    println("§6")
    val versions = listOf(Version(2, 4, 21), Version(2, 4, 0), Version(1, 9, 25))
    println(versions.sorted())
    println(biggest(Version(2, 4, 0), Version(2, 3, 99)))
}

// ⚠️ Gotcha: `T : Comparable<T>` reads "T can be compared with another T".
// The stdlib actually declares `sorted()` with `T : Comparable<T>` but
// `Comparator<in T>` elsewhere — that little `in` is lesson 25's topic.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Unbounded `<T>` (= `<T : Any?>`) only gives you toString/equals/hashCode.
//  • `<T : Bound>` limits allowed types AND unlocks Bound's members inside.
//  • Several bounds → `where T : A, T : B`.
//  • `<T : Any>` = "never a nullable type"; write `T?` where null is allowed.
//  • Implement Comparable / extend the bound and your types join in.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// I can only print it: 42
// §2
// 7
// banana
// 9
// null
// §3
// 2.5
// 2.0
// §4
// [apple, banana, fig, kiwi]
// §5
// 1
// key z not found
// §6
// [1.9.25, 2.4.0, 2.4.21]
// 2.4.0
