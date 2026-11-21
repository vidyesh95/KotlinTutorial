/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 04 — Strings and Chars
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : build text with templates, write multi-line raw strings, use the
 *         🆕 multi-dollar interpolation, slice/search/transform strings, work
 *         with single Chars, and build strings efficiently.
 *  Needs: lesson 03
 *  Time : ~30 min
 *  Next : lesson 05 — reading input
 *  Exercises: exercises/ex04/Ex04.kt
 */
package lesson04_strings_and_chars

fun main() {
    section1Templates()
    section2EscapesAndRawStrings()
    section3MultiDollarInterpolation()
    section4CommonOperations()
    section5Chars()
    section6Immutability()
    section7BuildingStrings()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. String templates
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A template is a birthday card with blanks: "Happy birthday, ____! You are
//    __ today." Kotlin fills each blank for you. `$name` fills a blank with a
//    variable, `${...}` fills it with the result of any expression.

fun section1Templates() {
    println("§1")
    val name = "Linus"
    val age = 8
    println("Happy birthday, $name! You are $age today.")
    println("Next year you'll be ${age + 1}. Name has ${name.length} letters.")
    println("Upper: ${name.uppercase()}")
    // Use ${} whenever the expression is more than a plain name:
    println("Wrong: $name.length   Right: ${name.length}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Escapes and raw strings
// ───────────────────────────────────────────────────────────────────────────
// Escapes in normal strings: \n newline, \t tab, \" quote, \\ backslash,
// \$ dollar sign, \u00E9 unicode character (é).
//
// Raw strings use triple quotes """ ... """: NO escapes, can span lines.
// Great for SQL, JSON, regexes. `.trimIndent()` removes the common leading
// indentation so the code can stay nicely indented.
//
// 🏗️ In the real world: you'll write SQL like this in Spring's JdbcClient:
//    """
//    SELECT id, title FROM book
//    WHERE author_id = :authorId
//    """.trimIndent()

fun section2EscapesAndRawStrings() {
    println("§2")
    println("Tab:\t|  Quote: \"  Backslash: \\  Dollar: \$5  Unicode: caf\u00E9")
    val sql = """
        SELECT id, title
        FROM book
        WHERE price < 20
    """.trimIndent()
    println(sql)
    val windowsPath = """C:\Users\ada"""      // backslashes are literal in raw strings
    println(windowsPath)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. 🆕 Multi-dollar interpolation (stable since Kotlin 2.2)
// ───────────────────────────────────────────────────────────────────────────
// Problem: raw strings have no escapes, so how do you write a literal `$`?
// Before: "${'$'}" — ugly. Now: put N dollar signs BEFORE the string. Then only
// N dollars in a row start a template; fewer dollars are plain text.
//
//     $$"""{"$schema": "...", "name": "$$name"}"""
//       → `$schema` stays as text, `$$name` is interpolated.
//
// 🏗️ In the real world: JSON Schema (`$schema`, `$ref`), MongoDB queries
//    (`$gt`), Spring property placeholders (`${server.port}`), shell scripts.

fun section3MultiDollarInterpolation() {
    println("§3")
    val name = "book"
    val json = $$"""{"$schema": "draft-07", "title": "$$name"}"""
    println(json)
    val springPlaceholder = $$"""port=${server.port:8080} app=$${name}"""
    println(springPlaceholder)
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Common operations
// ───────────────────────────────────────────────────────────────────────────
// Strings are indexed from 0. `s[0]` is the first Char, `s.length` the count.
// substring(start, endExclusive) — end index is NOT included.
// Kotlin's stdlib has lots of helpers; a few you'll use daily are below.

fun section4CommonOperations() {
    println("§4")
    val s = "Hello, Kotlin"
    println("${s.length} ${s[0]} ${s.last()}")
    println(s.substring(7))                       // from 7 to end
    println(s.substring(0, 5))                    // 0..4
    println(s.take(3) + "|" + s.takeLast(3))
    println("${s.contains("Kot")} ${s.startsWith("Hell")} ${s.indexOf('K')}")
    println(s.lowercase().replace("kotlin", "world"))
    println("  padded  ".trim() + "!")
    println("a,b,,c".split(","))
    println("7".padStart(3, '0'))                 // "007" — order numbers, times
    println("ab".repeat(3))
    println("racecar".reversed() == "racecar")
    println("".isEmpty().toString() + " " + "   ".isBlank())
    println("Kotlin".equals("KOTLIN", ignoreCase = true))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Chars
// ───────────────────────────────────────────────────────────────────────────
// A Char is ONE character in single quotes: 'a', '7', '\n'.
// "a" (String) and 'a' (Char) are different types!
// Chars have handy checks: isDigit(), isLetter(), isWhitespace(), isUpperCase().
// A String is iterable — you can loop over its Chars.

fun section5Chars() {
    println("§5")
    val c = 'k'
    println("${c.uppercaseChar()} ${c.isLetter()} ${'9'.isDigit()} ${c.code}")
    val word = "Kotlin2"
    val digits = word.count { it.isDigit() }
    val letters = word.filter { it.isLetter() }
    println("digits=$digits letters=$letters")
    for (ch in "abc") print("[$ch]")
    println()
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Strings are immutable; equality
// ───────────────────────────────────────────────────────────────────────────
// No function ever changes a String in place. `uppercase()`, `replace()`,
// `trim()` all return a NEW String; the original is untouched.
// Compare strings with `==` (content), never `===` (identity).
//
// ⚠️ Gotcha: `s.uppercase()` on its own line does nothing useful — you must
//    USE the result: `val loud = s.uppercase()`.

fun section6Immutability() {
    println("§6")
    val original = "quiet"
    val loud = original.uppercase()
    println("$original $loud")
    val a = "kot" + "lin"
    val b = "kotlin"
    println(a == b)
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Building strings efficiently
// ───────────────────────────────────────────────────────────────────────────
// Each `+` creates a new String. In a big loop that's lots of garbage.
// `buildString { }` gives you a mutable StringBuilder and returns the result.
// `joinToString` turns a collection into one string with separators.

fun section7BuildingStrings() {
    println("§7")
    val report = buildString {
        append("Items: ")
        for (i in 1..3) {
            append(i)
            if (i < 3) append(", ")
        }
        appendLine()
        append("Done")
    }
    println(report)
    println(listOf("a", "b", "c").joinToString(separator = " | ", prefix = "<", postfix = ">"))
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • "$name" and "${expr}" templates; \n \t \" \\ \$ escapes in normal strings.
//  • """raw strings""" + trimIndent() for SQL/JSON; 🆕 $$"""...""" when text needs `$`.
//  • Index from 0; substring end is exclusive; lots of helpers (split, padStart, take…).
//  • Strings are immutable — every operation returns a new String. Compare with ==.
//  • buildString / joinToString to assemble text.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Happy birthday, Linus! You are 8 today.
// Next year you'll be 9. Name has 5 letters.
// Upper: LINUS
// Wrong: Linus.length   Right: 5
// §2
// Tab:	|  Quote: "  Backslash: \  Dollar: $5  Unicode: café
// SELECT id, title
// FROM book
// WHERE price < 20
// C:\Users\ada
// §3
// {"$schema": "draft-07", "title": "book"}
// port=${server.port:8080} app=book
// §4
// 13 H n
// Kotlin
// Hello
// Hel|lin
// true true 7
// hello, world
// padded!
// [a, b, , c]
// 007
// ababab
// true
// true true
// true
// §5
// K true true 107
// digits=1 letters=Kotlin
// [a][b][c]
// §6
// quiet QUIET
// true
// §7
// Items: 1, 2, 3
// Done
// <a | b | c>
