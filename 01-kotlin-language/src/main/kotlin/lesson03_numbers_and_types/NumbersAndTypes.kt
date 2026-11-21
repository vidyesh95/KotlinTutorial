/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 03 — Numbers, Booleans, Chars and conversions
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : know Kotlin's basic types, write number literals, do arithmetic
 *         without surprises (integer division, overflow, floating-point),
 *         convert between types, and understand boxing / `==` vs `===`.
 *  Needs: lesson 02
 *  Time : ~35 min
 *  Next : lesson 04 — strings and chars
 *  Exercises: exercises/ex03/Ex03.kt
 */
package lesson03_numbers_and_types

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun main() {
    section1TheBasicTypes()
    section2Literals()
    section3Arithmetic()
    section4OverflowAndFloatingPoint()
    section5Conversions()
    section6Booleans()
    section7BoxingAndEquality()
    section8UnsignedAndBits()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The basic types
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Types are differently sized cups. A Byte is an espresso cup, an Int a mug,
//    a Long a big jug. A tiny cup holds tiny numbers; pour in too much and it
//    overflows. Double/Float are cups for numbers with a decimal point.
//
//   Type     Size     Range / meaning
//   Byte     8 bit    -128 .. 127
//   Short    16 bit   -32 768 .. 32 767
//   Int      32 bit   about ±2.1 billion        ← default for whole numbers
//   Long     64 bit   about ±9.2 quintillion
//   Float    32 bit   ~6-7 significant digits
//   Double   64 bit   ~15-16 significant digits ← default for decimals
//   Boolean           true / false
//   Char     16 bit   one UTF-16 character, written 'a'
//
// In Kotlin everything is an object you can call functions on (`5.plus(3)`),
// yet on the JVM `Int` compiles to a fast primitive `int` whenever possible.

fun section1TheBasicTypes() {
    println("§1")
    println("Byte ${Byte.MIN_VALUE}..${Byte.MAX_VALUE}")
    println("Int max = ${Int.MAX_VALUE}")
    println("Long max = ${Long.MAX_VALUE}")
    println("5.plus(3) = ${5.plus(3)}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Literals — writing numbers in code
// ───────────────────────────────────────────────────────────────────────────
//   1_000_000    underscores for readability (ignored by the compiler)
//   42L          Long          0xFF   hexadecimal (255)
//   2.5          Double        0b1010 binary (10)
//   2.5f         Float         'A'    Char
//   1e3          Double 1000.0 (scientific notation)
// No octal literals in Kotlin.

fun section2Literals() {
    println("§2")
    val population = 8_100_000_000L      // too big for Int → needs L
    val mask = 0xFF
    val flags = 0b1010
    val price = 2.5f
    val kilo = 1e3
    println("$population $mask $flags $price $kilo")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Arithmetic: + - * / %
// ───────────────────────────────────────────────────────────────────────────
// ⚠️ Gotcha: Int / Int is INTEGER division — the fraction is thrown away
//    (rounded toward zero). Make one side a Double to get decimals.
// `%` is the remainder; its sign follows the LEFT operand. If you need a
// result that's always ≥ 0 (e.g. clock arithmetic), use `.mod()`.

fun section3Arithmetic() {
    println("§3")
    println("7 / 2 = ${7 / 2}")
    println("7.0 / 2 = ${7.0 / 2}")
    println("7 % 2 = ${7 % 2}")
    println("-7 / 2 = ${-7 / 2}, -7 % 2 = ${-7 % 2}, (-7).mod(2) = ${(-7).mod(2)}")
    println("2^10 = ${2.0.pow(10)}, sqrt(81) = ${sqrt(81.0)}, abs(-4) = ${abs(-4)}")

    var counter = 0
    counter++                 // increment by 1
    counter += 10             // compound assignment
    println("counter = $counter")

    // Prefix vs postfix: both add 1 to the variable — they differ in what the
    // EXPRESSION itself evaluates to.
    //   x++  → gives the OLD value, then increments ("use, then add")
    //   ++x  → increments first, gives the NEW value ("add, then use")
    var x = 5
    val usedThenAdded = x++   // usedThenAdded = 5, x is now 6
    val addedThenUsed = ++x   // x is now 7, addedThenUsed = 7
    println("x++ gave $usedThenAdded, ++x gave $addedThenUsed, x = $x")
    // Same for x-- and --x. Tip: as a standalone statement (`x++`) they're
    // identical; avoid using them inside bigger expressions — it hurts readability.
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Overflow and floating-point surprises
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A car's odometer with 6 digits shows 999999. Drive one more km and it
//    flips to 000000. Int does the same at Int.MAX_VALUE: it wraps around to
//    the most NEGATIVE value — silently, no error!
//
// Doubles store numbers in binary, and many decimals (like 0.1) can't be
// stored exactly — just like 1/3 can't be written exactly in decimal.
//
// 🏗️ In the real world: NEVER use Double for money. Use whole cents in a Long,
//    or java.math.BigDecimal. `Math.addExact(a, b)` throws instead of wrapping.

fun section4OverflowAndFloatingPoint() {
    println("§4")
    val max = Int.MAX_VALUE
    val wrapped = max.plus(1)
    println("Int.MAX_VALUE + 1 = $wrapped")
    println("0.1 + 0.2 = ${0.1 + 0.2}")

    val zero = 0.0
    println("1.0 / 0.0 = ${1.0 / zero}")     // Doubles give Infinity; Int / 0 would crash
    val priceInCents = 1999L                   // $19.99 stored safely
    println("total for 3 = ${priceInCents * 3} cents")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Conversions — always explicit
// ───────────────────────────────────────────────────────────────────────────
// Kotlin NEVER converts number types silently. An Int is not a Long:
//
//     val i: Int = 1
//     val l: Long = i          // ❌ Type mismatch
//     val l: Long = i.toLong() // ✅
//
// Every number type has toByte(), toShort(), toInt(), toLong(), toFloat(),
// toDouble(). Strings have toInt(), toDouble()... (crash on bad input) and
// toIntOrNull()... (return null instead — lesson 06).
// Double → Int: `toInt()` cuts off the fraction; `roundToInt()` rounds.
// In mixed arithmetic the result takes the "bigger" type: Int + Long = Long.

fun section5Conversions() {
    println("§5")
    val i = 1
    val l: Long = i.toLong()
    val mixed = i + l                         // Long
    println("mixed is ${mixed::class.simpleName}")
    println("3.7.toInt() = ${3.7.toInt()}, 3.7.roundToInt() = ${3.7.roundToInt()}")
    println("\"42\".toInt() + 1 = ${"42".toInt() + 1}")
    println("'A'.code = ${'A'.code}, Char(66) = ${Char(66)}")
    println("'7'.digitToInt() = ${'7'.digitToInt()}, 'a' + 1 = ${'a' + 1}")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Booleans and logic
// ───────────────────────────────────────────────────────────────────────────
//   &&  and   — true only if BOTH are true
//   ||  or    — true if AT LEAST ONE is true
//   !   not   — flips true/false
// `&&` and `||` are LAZY ("short-circuit"): if the left side already decides
// the answer, the right side isn't evaluated at all.
// Comparisons produce Booleans: == != < > <= >=

fun section6Booleans() {
    println("§6")
    val age = 20
    val hasTicket = true
    println("can enter: ${age >= 18 && hasTicket}")
    println("free entry: ${age < 6 || age > 65}")
    println("not adult: ${!(age >= 18)}")

    var calls = 0
    fun expensiveCheck(): Boolean { calls++; return true }
    val result = false && expensiveCheck()    // right side skipped!
    println("result=$result, expensiveCheck called $calls times")
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Boxing, `==` and `===`
// ───────────────────────────────────────────────────────────────────────────
// `==`  STRUCTURAL equality: "do they hold the same value?" (calls equals()).
// `===` REFERENTIAL equality: "are they the very same object in memory?"
//
// On the JVM, a plain `Int` is a raw primitive. But an `Int?` (or an Int inside
// a List) must be an OBJECT, so the JVM "boxes" it into java.lang.Integer.
// The JVM caches boxes for -128..127 and creates NEW boxes for bigger numbers.
// So with `===`, `Int?` 100 vs 100 would be the same box, but 10000 vs 10000
// two different boxes — an accident of caching, not of math!
//
// That's why Kotlin 2 REFUSES `===` on number types (it's a compile-time
// warning/error: "Identity equality for arguments of types Int? and Int? is
// prohibited"). For numbers always use `==`.
//
// 🧒 Like I'm 5:
//    Two kids each hold a red ball. `==` asks "are both balls red?" → yes.
//    `===` asks "are they holding the SAME ball?" → no, two balls.

fun section7BoxingAndEquality() {
    println("§7")
    val small: Int? = 100
    val anotherSmall: Int? = 100
    val big: Int? = 10_000
    val anotherBig: Int? = 10_000
    println("100 == 100: ${small == anotherSmall}")
    println("10000 == 10000: ${big == anotherBig}")

    val a = StringBuilder("x")                 // objects WITHOUT value semantics
    val b = StringBuilder("x")
    val c = a
    println("a === b: ${a === b}, a === c: ${a === c}")
}

// ───────────────────────────────────────────────────────────────────────────
// §8. Unsigned numbers and bitwise operations
// ───────────────────────────────────────────────────────────────────────────
// UByte, UShort, UInt, ULong hold only ≥ 0 values, so they reach twice as high.
// Literal suffix `u` (or `uL`). Handy for bytes from files/network, colors, hashes.
//
// Bitwise operations are named functions in Kotlin (written infix):
//   and, or, xor, inv(), shl (shift left), shr (shift right, keeps sign),
//   ushr (shift right, fills with 0)
// 🏗️ In the real world: permission flags (READ=1, WRITE=2, EXEC=4) combined
//    with `or` and checked with `and`.

fun section8UnsignedAndBits() {
    println("§8")
    val big: UInt = 4_000_000_000u              // wouldn't fit in Int
    val minusOne = -1
    println("UInt: $big, (-1).toUInt() = ${minusOne.toUInt()}")

    println("1100 and 1010 = ${0b1100 and 0b1010}")
    println("1100 or 1010 = ${0b1100 or 0b1010}")
    println("1100 xor 1010 = ${0b1100 xor 0b1010}")
    println("1 shl 4 = ${1 shl 4}, 5.inv() = ${5.inv()}")

    val read = 1
    val write = 2
    val perms = read or write
    println("can write: ${perms and write != 0}")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Int and Double are the defaults; use L / f / u suffixes for Long / Float / unsigned.
//  • Int / Int drops the fraction. Int overflow wraps silently. Doubles are inexact.
//  • Conversions are explicit: toLong(), toInt(), roundToInt(), toIntOrNull().
//  • `&&`/`||` short-circuit. Use `==` for values; `===` only for object identity.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Byte -128..127
// Int max = 2147483647
// Long max = 9223372036854775807
// 5.plus(3) = 8
// §2
// 8100000000 255 10 2.5 1000.0
// §3
// 7 / 2 = 3
// 7.0 / 2 = 3.5
// 7 % 2 = 1
// -7 / 2 = -3, -7 % 2 = -1, (-7).mod(2) = 1
// 2^10 = 1024.0, sqrt(81) = 9.0, abs(-4) = 4
// counter = 11
// x++ gave 5, ++x gave 7, x = 7
// §4
// Int.MAX_VALUE + 1 = -2147483648
// 0.1 + 0.2 = 0.30000000000000004
// 1.0 / 0.0 = Infinity
// total for 3 = 5997 cents
// §5
// mixed is Long
// 3.7.toInt() = 3, 3.7.roundToInt() = 4
// "42".toInt() + 1 = 43
// 'A'.code = 65, Char(66) = B
// '7'.digitToInt() = 7, 'a' + 1 = b
// §6
// can enter: true
// free entry: false
// not adult: false
// result=false, expensiveCheck called 0 times
// §7
// 100 == 100: true
// 10000 == 10000: true
// a === b: false, a === c: true
// §8
// UInt: 4000000000, (-1).toUInt() = 4294967295
// 1100 and 1010 = 8
// 1100 or 1010 = 14
// 1100 xor 1010 = 6
// 1 shl 4 = 16, 5.inv() = -6
// can write: true
