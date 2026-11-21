/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 33 — Regular expressions: patterns for text
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : read and write common regex patterns, and use Kotlin's Regex API:
 *         matches, containsMatchIn, find, findAll, groups, replace, split.
 *  Needs: lesson 04 (strings), 12 (lambdas)
 *  Time : ~35 min
 *  Next : lesson 34 — files and I/O
 *  Exercises: exercises/ex33/Ex33.kt
 */
package lesson33_regex

fun main() {
    section1WhatIsRegex()
    section2BuildingBlocks()
    section3MatchingFunctions()
    section4Groups()
    section5ReplaceAndSplit()
    section6Validators()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. What is a regular expression?
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A regex is a "wanted poster" for text. Instead of describing ONE exact
//    word, it describes what the word LOOKS like: "starts with a capital
//    letter, then 2–3 digits". Kotlin then searches text for anything that
//    fits the poster.
//
// Write patterns in RAW strings ("""...""") so backslashes stay as typed:
// "\\d+" (normal string) and """\d+""" (raw string) are the same pattern.

fun section1WhatIsRegex() {
    println("§1")
    val digits = Regex("""\d+""")              // one or more digits
    println(digits.containsMatchIn("Order 66 shipped"))
    println(digits.find("Order 66 shipped")?.value)
    println("""\d+""".toRegex().matches("2026"))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Building blocks (the poster vocabulary)
// ───────────────────────────────────────────────────────────────────────────
//   Characters      .  any char      \d digit     \w letter/digit/_     \s whitespace
//                   \D \W \S = NOT digit / word / space
//   Sets            [abc] one of a,b,c     [a-z] range     [^0-9] anything but a digit
//   Quantifiers     ?  0 or 1      *  0 or more      +  1 or more
//                   {3} exactly 3   {2,4} 2 to 4    {2,} 2 or more
//   Anchors         ^ start of text   $ end of text   \b word boundary
//   Alternation     cat|dog   — either side
//   Groups          ( … )  — capture a part;  (?<name> … )  — capture with a name
//   Escaping        \.  \(  \+  — when you mean the literal character

fun section2BuildingBlocks() {
    println("§2")
    val text = "cat bat rat cart"
    println(Regex("""[cb]at""").findAll(text).map { it.value }.toList())
    println(Regex("""\b\w{3}\b""").findAll(text).map { it.value }.toList())   // exactly-3-letter words
    println(Regex("""colou?r""").findAll("color colour colouur").map { it.value }.toList())
    println(Regex("""^\d{3}-\d{4}$""").matches("555-1234"))
    println(Regex("""^\d{3}-\d{4}$""").matches("call 555-1234"))              // ^ and $ demand the WHOLE text
    println(Regex("""v\d+\.\d+""").find("Kotlin v2.4 rocks")?.value)            // \. = a real dot
}

// ───────────────────────────────────────────────────────────────────────────
// §3. The four ways to ask
// ───────────────────────────────────────────────────────────────────────────
//   regex.matches(text)          — does the WHOLE text fit?          → Boolean
//   regex.containsMatchIn(text)  — is there a fit ANYWHERE inside?   → Boolean
//   regex.find(text)             — the FIRST fit                      → MatchResult?
//   regex.findAll(text)          — ALL fits, lazily                   → Sequence<MatchResult>

fun section3MatchingFunctions() {
    println("§3")
    val year = Regex("""\d{4}""")
    val text = "Kotlin 1.0 in 2016, Kotlin 2.0 in 2024"
    println(year.matches(text))
    println(year.containsMatchIn(text))
    val first = year.find(text)
    println("${first?.value} at index ${first?.range}")
    println(year.findAll(text).map { it.value }.joinToString())
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Groups — pulling pieces out
// ───────────────────────────────────────────────────────────────────────────
// Parentheses capture parts of a match. groupValues[0] is the whole match,
// [1], [2], … are the groups. Named groups (?<name>…) are clearer.

fun section4Groups() {
    println("§4")
    val date = Regex("""(\d{4})-(\d{2})-(\d{2})""")
    val match = date.find("Released on 2026-11-22.")!!
    println(match.groupValues)

    val (year, month, day) = match.destructured   // destructuring the groups
    println("day=$day month=$month year=$year")

    val log = Regex("""(?<level>INFO|WARN|ERROR) \[(?<module>\w+)] (?<message>.+)""")
    val entry = log.find("12:00:01 ERROR [auth] Bad password for ada")!!
    println(entry.groups["level"]?.value)
    println(entry.groups["module"]?.value)
    println(entry.groups["message"]?.value)
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Replace and split
// ───────────────────────────────────────────────────────────────────────────
// `replace` accepts a fixed replacement ("$1" refers to group 1) or a lambda
// that computes each replacement from the match.

fun section5ReplaceAndSplit() {
    println("§5")
    println("too    many     spaces".replace(Regex("""\s+"""), " "))
    println("2026-11-22".replace(Regex("""(\d{4})-(\d{2})-(\d{2})"""), "$3/$2/$1"))
    println("price: 10 EUR, tax: 2 EUR".replace(Regex("""\d+""")) { (it.value.toInt() * 100).toString() })
    println("a, b;c ,d".split(Regex("""\s*[,;]\s*""")))
    val card = "4111 1111 1111 1234"
    println(card.replace(Regex("""\d(?=(?:\s?\d){4})"""), "*"))   // mask all but the last 4 digits
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Practical validators
// ───────────────────────────────────────────────────────────────────────────
// 🏗️ In the real world: request validation (module 04) uses patterns like these.
// Keep compiled Regex objects in top-level vals — compiling is the slow part.

private val SLUG = Regex("""^[a-z0-9]+(?:-[a-z0-9]+)*$""")
private val SIMPLE_EMAIL = Regex("""^[^@\s]+@[^@\s]+\.[a-z]{2,}$""", RegexOption.IGNORE_CASE)

fun isSlug(text: String) = SLUG.matches(text)
fun looksLikeEmail(text: String) = SIMPLE_EMAIL.matches(text)

fun section6Validators() {
    println("§6")
    println(listOf("kotlin-tutorial", "Kotlin Tutorial", "a--b", "lesson-33").map(::isSlug))
    println(listOf("ada@example.com", "ADA@EXAMPLE.ORG", "ada@", "a b@c.de").map(::looksLikeEmail))
}

// ⚠️ Gotcha 1: an email regex can only say "looks plausible". The only real
// check is sending a verification email (module 06 does this).
//
// ⚠️ Gotcha 2 — catastrophic backtracking: nested quantifiers like (a+)+ or
// (\w+\s?)+$ can take EXPONENTIAL time on some inputs that almost match.
// An attacker can send such input and freeze your server (ReDoS). Avoid
// nesting `+`/`*` inside another `+`/`*`, and limit input length before matching.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Write patterns as raw strings: Regex("""\d+""").
//  • matches = whole text, containsMatchIn = anywhere, find = first, findAll = all.
//  • Groups ( ) capture parts: groupValues, destructured, named groups (?<n>…).
//  • replace with "$1" or a lambda; split with a pattern.
//  • Precompile shared regexes; beware nested quantifiers (ReDoS).
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// true
// 66
// true
// §2
// [cat, bat]
// [cat, bat, rat]
// [color, colour]
// true
// false
// v2.4
// §3
// false
// true
// 2016 at index 14..17
// 2016, 2024
// §4
// [2026-11-22, 2026, 11, 22]
// day=22 month=11 year=2026
// ERROR
// auth
// Bad password for ada
// §5
// too many spaces
// 22/11/2026
// price: 1000 EUR, tax: 200 EUR
// [a, b, c, d]
// **** **** **** 1234
// §6
// [true, false, false, true]
// [true, true, false, false]
