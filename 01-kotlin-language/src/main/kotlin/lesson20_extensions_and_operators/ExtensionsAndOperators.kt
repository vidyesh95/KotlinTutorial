/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 20 — Extensions, infix functions, operator overloading
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : add functions to types you don't own, write readable `a to b`
 *         style calls, and make your own types work with + - [] in < ..
 *  Needs: lessons 15–19
 *  Time : ~35 min
 *  Next : lesson 21 — delegation
 *  Exercises: exercises/ex20/Ex20.kt
 */
package lesson20_extensions_and_operators

fun main() {
    section1ExtensionFunctions()
    section2ExtensionsAreStatic()
    section3NullableReceiversAndProperties()
    section4Infix()
    section5ArithmeticOperators()
    section6IndexingContainsInvoke()
    section7ComparisonRangesAndDestructuring()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Extension functions
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Your friend's toy car can't honk. You can't open it up and rebuild it —
//    it's not yours. But you CAN put a sticker-button on it that says "honk"
//    and makes the noise. The car is unchanged; you just added a button from
//    the outside.
//
// Precisely: `fun String.shout(): String` adds a function you call like a
// member — `"hi".shout()`. Inside, `this` is the receiver ("hi").
// You can't touch private members: you're still outside the class.
//
// 🏗️ In the real world: the Kotlin standard library is mostly extensions on
//    Java types (`String.lowercase()`, `List.filter`). Spring's Kotlin support
//    adds extensions like `jdbcClient.sql(...).query<Book>()`.

fun String.shout(): String = uppercase() + "!"           // `this` is implicit
fun String.initials(): String = split(" ").joinToString("") { it.first().uppercase() }
fun Int.isEven(): Boolean = this % 2 == 0

fun section1ExtensionFunctions() {
    println("§1")
    println("hello".shout())
    println("ada king lovelace".initials())
    println((1..6).filter { it.isEven() })
}

// ───────────────────────────────────────────────────────────────────────────
// §2. ⚠️ Extensions are resolved STATICALLY
// ───────────────────────────────────────────────────────────────────────────
// An extension is just a function with a hidden first parameter. Which one runs
// is decided by the DECLARED type of the variable at compile time — not by the
// real object at runtime (unlike `override`). And if a class already has a
// member with the same signature, the MEMBER always wins.

open class Shape
class Square : Shape()

fun Shape.name() = "shape"
fun Square.name() = "square"

class Robot {
    fun speak() = "member speak"
}

// The compiler warns "Extension is shadowed by a member" — we silence it here on purpose to show the effect.
@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
fun Robot.speak() = "extension speak"                     // shadowed: never called

fun section2ExtensionsAreStatic() {
    println("§2")
    val asShape: Shape = Square()
    println(asShape.name())                                 // "shape" — declared type is Shape!
    println(Square().name())
    println(Robot().speak())
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Nullable receivers and extension properties
// ───────────────────────────────────────────────────────────────────────────
// `fun String?.orDash()` can be called on null — inside, `this` may be null.
// That's how the stdlib's `isNullOrBlank()` works.
//
// Extension PROPERTIES compute a value; they can't store anything (there's no
// place to put a backing field in someone else's class).

fun String?.orDash(): String = if (this.isNullOrBlank()) "-" else this

val String.wordCount: Int
    get() = trim().split(Regex("\\s+")).count { it.isNotEmpty() }

val <T> List<T>.secondOrNull: T?                           // generic extension property
    get() = getOrNull(1)

fun section3NullableReceiversAndProperties() {
    println("§3")
    val missing: String? = null
    println(missing.orDash())
    println("Ada".orDash())
    println("  the quick   brown fox ".wordCount)
    println(listOf("a", "b").secondOrNull)
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Infix functions — read like English
// ───────────────────────────────────────────────────────────────────────────
// A member or extension function with exactly ONE parameter, marked `infix`,
// can be called without the dot and parentheses: `a add b`.
// You already know one: `1 to "one"` is `1.to("one")`.
//
// ⚠️ Gotcha: infix calls have LOWER precedence than arithmetic.
//    `1 add 2 * 3` means `1 add (2 * 3)` = 7.

infix fun Int.add(other: Int): Int = this + other

data class Person(val name: String)

infix fun Person.likes(other: Person): String = "$name likes ${other.name}"

fun section4Infix() {
    println("§4")
    println(10 add 20)
    println(1 add 2 * 3)
    println(Person("Ada") likes Person("Charles"))
    val pair = "key" to 42
    println(pair)
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Operator overloading: + - * and unary -
// ───────────────────────────────────────────────────────────────────────────
// Operators are just functions with FIXED names, marked `operator`:
//   a + b → a.plus(b)      a - b → a.minus(b)     a * b → a.times(b)
//   -a    → a.unaryMinus() a++   → a.inc()        a += b → a.plusAssign(b) or a = a.plus(b)
//
// 🧒 Like I'm 5: `+` is a nickname. When you write `a + b`, Kotlin quietly
//    phones `a.plus(b)`. Teach your class a `plus` and `+` just works.

data class Vec(val x: Int, val y: Int) {
    operator fun plus(other: Vec) = Vec(x + other.x, y + other.y)
    operator fun minus(other: Vec) = Vec(x - other.x, y - other.y)
    operator fun times(factor: Int) = Vec(x * factor, y * factor)
    operator fun unaryMinus() = Vec(-x, -y)
    operator fun inc() = Vec(x + 1, y + 1)
}

operator fun Int.times(v: Vec) = v * this                  // makes `3 * vec` work too (extension operator)

fun section5ArithmeticOperators() {
    println("§5")
    var position = Vec(10, 20)
    val step = Vec(1, 2)
    println(position + step)
    println(position - step)
    println(step * 3)
    println(3 * step)
    println(-position)
    position += step                                       // position = position.plus(step)
    position++                                             // position = position.inc()
    println(position)
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Indexing, `in`, and calling an object like a function
// ───────────────────────────────────────────────────────────────────────────
//   a[i]       → a.get(i)            a[i] = v → a.set(i, v)
//   x in a     → a.contains(x)       a(args)  → a.invoke(args)

class Grid(private val width: Int, height: Int) {
    private val cells = CharArray(width * height) { '.' }

    operator fun get(row: Int, col: Int): Char = cells[row * width + col]
    operator fun set(row: Int, col: Int, value: Char) {
        cells[row * width + col] = value
    }

    operator fun contains(value: Char): Boolean = value in cells
    override fun toString() = cells.concatToString().chunked(width).joinToString("\n")
}

class Multiplier(private val factor: Int) {
    operator fun invoke(value: Int): Int = value * factor   // object behaves like a function
}

fun section6IndexingContainsInvoke() {
    println("§6")
    val grid = Grid(width = 3, height = 2)
    grid[0, 1] = '#'
    grid[1, 2] = '@'
    println(grid)
    println("grid[0, 1] = ${grid[0, 1]}")
    println("'@' in grid: ${'@' in grid}, 'X' in grid: ${'X' in grid}")

    val triple = Multiplier(3)
    println(triple(14))                                     // triple.invoke(14)
}

// ───────────────────────────────────────────────────────────────────────────
// §7. compareTo, ranges, and destructuring
// ───────────────────────────────────────────────────────────────────────────
//   a < b, a >= b … → a.compareTo(b) < 0 …   (implement Comparable<T>)
//   a..b           → a.rangeTo(b)
//   val (x, y) = p → p.component1(), p.component2()  (data classes get these free)

data class Version(val major: Int, val minor: Int) : Comparable<Version> {
    override operator fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor)

    override fun toString() = "$major.$minor"
}

class Money(private val cents: Long) {
    operator fun component1() = cents / 100                 // whole units
    operator fun component2() = cents % 100                 // remainder
}

fun section7ComparisonRangesAndDestructuring() {
    println("§7")
    val v1 = Version(1, 9)
    val v2 = Version(2, 0)
    println("$v1 < $v2: ${v1 < v2}")
    println("max: ${maxOf(v1, v2)}")
    println("sorted: ${listOf(v2, Version(1, 10), v1).sorted()}")
    val supported = Version(1, 0)..Version(1, 99)            // Comparable gives rangeTo for free
    println("1.9 supported? ${v1 in supported}, 2.0 supported? ${v2 in supported}")

    val (euros, cents) = Money(1_234)
    println("$euros euros and $cents cents")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `fun Type.name()` adds a function from outside; `this` = receiver; no private access.
//  • Extensions are chosen by the DECLARED type at compile time; members always win.
//  • Nullable receivers (`String?.f()`) and computed extension properties are possible.
//  • `infix` lets one-argument functions read like `a likes b`.
//  • `operator fun plus/minus/times/get/set/contains/invoke/compareTo/componentN`
//    maps symbols to function calls. Use only where the meaning is obvious.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// HELLO!
// AKL
// [2, 4, 6]
// §2
// shape
// square
// member speak
// §3
// -
// Ada
// 4
// b
// §4
// 30
// 7
// Ada likes Charles
// (key, 42)
// §5
// Vec(x=11, y=22)
// Vec(x=9, y=18)
// Vec(x=3, y=6)
// Vec(x=3, y=6)
// Vec(x=-10, y=-20)
// Vec(x=12, y=23)
// §6
// .#.
// ..@
// grid[0, 1] = #
// '@' in grid: true, 'X' in grid: false
// 42
// §7
// 1.9 < 2.0: true
// max: 2.0
// sorted: [1.9, 1.10, 2.0]
// 1.9 supported? true, 2.0 supported? false
// 12 euros and 34 cents
