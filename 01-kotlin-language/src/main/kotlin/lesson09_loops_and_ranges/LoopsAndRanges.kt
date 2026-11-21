/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 09 — Repeating things: loops and ranges
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : build ranges (`..`, `..<`, `downTo`, `step`), loop with `for`,
 *         `while`, `do-while`, `repeat`, `forEach`, and control loops with
 *         `break` / `continue` and labels.
 *  Needs: lesson 08 (if/when)
 *  Time : ~30 min
 *  Next : lesson 10 — exceptions and Result
 *  Exercises: exercises/ex09/Ex09.kt
 */
package lesson09_loops_and_ranges

fun main() {
    section1Ranges()
    section2ForLoops()
    section3WhileAndDoWhile()
    section4RepeatAndForEach()
    section5BreakAndContinue()
    section6Labels()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Ranges
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A range is a hopscotch court: "squares 1 to 10". You can hop on every
//    square, every 2nd square (`step 2`), or walk back (`downTo`).
//
//   1..5        → 1 2 3 4 5        (both ends INCLUDED)
//   1..<5       → 1 2 3 4          (end EXCLUDED — perfect for indexes; old name: `until`)
//   5 downTo 1  → 5 4 3 2 1
//   1..10 step 3 → 1 4 7 10
// `in` / `!in` check membership. Ranges also work for Char ('a'..'z').

fun show(range: IntProgression): String = range.joinToString(" ")

fun section1Ranges() {
    println("§1")
    println(show(1..5))
    println(show(1..<5))
    println(show(5 downTo 1))
    println(show(1..10 step 2))
    println(show(9 downTo 0 step 3))

    val x = 10
    println("x in 1..10? ${x in 1..10}")
    val list = listOf("a", "b", "c")
    println("-1 is a valid index? ${-1 in list.indices}")   // indices == 0..2
    println("vowel? ${'e' in "aeiou"}, letter? ${'q' in 'a'..'z'}")
    println((1..4).sum())                                    // ranges are collections-like
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `for` loops
// ───────────────────────────────────────────────────────────────────────────
// `for (item in something)` works on anything iterable: ranges, lists, sets,
// maps, strings…  There is NO C-style `for (i = 0; i < n; i++)` in Kotlin —
// you use a range instead.

fun section2ForLoops() {
    println("§2")
    val items = listOf("apple", "mango", "orange")

    for (item in items) print("$item ")
    println()

    for (index in items.indices) print("${index}=${items[index]} ")   // by index
    println()

    for ((index, item) in items.withIndex()) print("#$index $item ")  // both at once, nicer
    println()

    val stock = mapOf("apple" to 3, "mango" to 0)
    for ((fruit, count) in stock) println("$fruit: $count")            // destructure map entries

    for (ch in "hey") print("[$ch]")                                   // a String is a sequence of Chars
    println()
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `while` and `do-while`
// ───────────────────────────────────────────────────────────────────────────
// `while (cond) { … }`  checks FIRST → may run zero times.
// `do { … } while (cond)` checks AFTER → always runs at least once.
// Use them when you don't know in advance how many steps you need.

fun section3WhileAndDoWhile() {
    println("§3")
    var n = 27
    var steps = 0
    while (n != 1) {                       // Collatz: halve if even, else 3n+1
        n = if (n % 2 == 0) n / 2 else 3 * n + 1
        steps++
    }
    println("27 reaches 1 after $steps steps")

    var i = 1
    do {
        print("7×$i=${7 * i} ")
        i++
    } while (i <= 3)
    println()

    var neverTrue = 100
    do {
        println("do-while ran once even though $neverTrue > 10")
    } while (neverTrue++ < 10)
}

// ───────────────────────────────────────────────────────────────────────────
// §4. `repeat` and `forEach`
// ───────────────────────────────────────────────────────────────────────────
// `repeat(n) { i -> … }` — "do this n times".
// `list.forEach { … }` — a function taking a lambda (lesson 12). Handy in
// chains, but a plain `for` is clearer when you need break/continue.

fun section4RepeatAndForEach() {
    println("§4")
    repeat(3) { i -> print("hip$i ") }
    println("hooray!")
    listOf("apple", "banana", "orange").forEach { println(it) }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. `break` and `continue`
// ───────────────────────────────────────────────────────────────────────────
// break    → leave the loop right now.
// continue → skip the rest of THIS round, go to the next one.

fun section5BreakAndContinue() {
    println("§5")
    for (i in 1..10) {
        if (i == 5) continue          // skip 5
        if (i == 8) break             // stop completely at 8
        print("$i ")
    }
    println()

    // Find the first number divisible by both 6 and 4.
    var found = -1
    for (candidate in 1..100) {
        if (candidate % 6 == 0 && candidate % 4 == 0) {
            found = candidate
            break
        }
    }
    println("first common multiple: $found")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Labels: break out of an OUTER loop
// ───────────────────────────────────────────────────────────────────────────
// A plain `break` only exits the innermost loop. Put a label (`outer@`) on a
// loop, then `break@outer` / `continue@outer` targets that loop.
//
// 🧒 Like I'm 5:
//    You're searching rooms (outer loop) and drawers in each room (inner loop)
//    for your toy. Found it? You don't just close this drawer — you leave the
//    whole house. `break@house`.

fun section6Labels() {
    println("§6")
    val grid = listOf(
        listOf(1, 2, 3),
        listOf(4, 99, 6),
        listOf(7, 8, 9),
    )
    outer@ for ((row, cells) in grid.withIndex()) {
        for ((col, value) in cells.withIndex()) {
            if (value == 99) {
                println("found 99 at row=$row col=$col")
                break@outer                       // leaves BOTH loops
            }
        }
    }

    rows@ for (i in 1..3) {
        for (j in 1..3) {
            if (j == 2) continue@rows             // skip rest of this row, go to next i
            print("($i,$j) ")
        }
    }
    println()
}

// ⚠️ Gotcha: `for (i in 0..list.size)` goes ONE TOO FAR (crashes with
// IndexOutOfBoundsException). Use `0..<list.size` or better `list.indices`.

// 🏗️ In the real world: in backend code you'll mostly use collection functions
// (map/filter, lesson 07 & 13) instead of hand-written loops; `while` shows up
// for things like "keep reading pages until there are no more".

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `a..b` inclusive, `a..<b` exclusive end, `downTo` backwards, `step` to skip.
//  • `for (x in thing)` loops over any iterable; `withIndex()` gives index + value.
//  • `while` may run 0 times, `do-while` runs at least once, `repeat(n)` n times.
//  • `break` leaves, `continue` skips; `label@` + `break@label` for nested loops.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 1 2 3 4 5
// 1 2 3 4
// 5 4 3 2 1
// 1 3 5 7 9
// 9 6 3 0
// x in 1..10? true
// -1 is a valid index? false
// vowel? true, letter? true
// 10
// §2
// apple mango orange
// 0=apple 1=mango 2=orange
// #0 apple #1 mango #2 orange
// apple: 3
// mango: 0
// [h][e][y]
// §3
// 27 reaches 1 after 111 steps
// 7×1=7 7×2=14 7×3=21
// do-while ran once even though 100 > 10
// §4
// hip0 hip1 hip2 hooray!
// apple
// banana
// orange
// §5
// 1 2 3 4 6 7
// first common multiple: 12
// §6
// found 99 at row=1 col=1
// (1,1) (2,1) (3,1)
