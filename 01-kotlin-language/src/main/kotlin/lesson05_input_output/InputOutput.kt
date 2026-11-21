/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 05 — Input and output: talking to the user
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : read lines the user types with `readln()` / `readlnOrNull()`,
 *         turn text into numbers safely, split a line into pieces, and read
 *         until the input ends.
 *  Needs: lessons 03–04
 *  Time : ~20 min
 *  Next : lesson 06 — null safety
 *  Exercises: exercises/ex05/Ex05.kt
 *
 *  ▶ When you run this in IntelliJ, TYPE the lines from the `// Input:` block
 *    at the bottom (press Enter after each). To signal "end of input" press
 *    Ctrl+D (macOS/Linux) or Ctrl+Z then Enter (Windows).
 *    The automated test feeds those lines for you.
 */
package lesson05_input_output

fun main() {
    section1Output()
    section2ReadALine()
    section3NumbersFromText()
    section4SplittingALine()
    section5ReadUntilTheEnd()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Output recap
// ───────────────────────────────────────────────────────────────────────────
// Standard output (stdout) is where `print`/`println` write — usually the
// console. There's also standard error (stderr) for problems: `System.err.println`.
// 🏗️ In the real world: servers don't println — they use a LOGGER
//    (`log.info("...")`) which adds time, level and thread, and can go to files.

fun section1Output() {
    println("§1")
    val apples = 3
    val pears = 2
    print("apples=$apples ")
    println("pears=$pears total=${apples + pears}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Reading a line: readln()
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    `readln()` is the program holding out its hand and waiting. It does
//    NOTHING until you type a line and press Enter; then it takes the whole
//    line (as a String) and carries on.
//
// `readln()` always returns a String. If the input has already ended it
// throws an exception. `readlnOrNull()` returns null instead (lesson 06 is all
// about handling null).

fun section2ReadALine() {
    println("§2")
    println("What's your name?")
    val name = readln()
    println("Hello, $name!")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Turning text into numbers
// ───────────────────────────────────────────────────────────────────────────
// Input is ALWAYS text. "42" is not 42 — convert it:
//   • `toInt()`        — crashes (NumberFormatException) on "abc"
//   • `toIntOrNull()`  — gives null on "abc", so you can react nicely
//
// ⚠️ Gotcha: never trust user input. Prefer the ...OrNull() versions and
//    decide what to do when the value is bad.
// 🏗️ In the real world: in Spring you rarely parse by hand — request bodies
//    are converted to objects for you — but validation is still your job.

fun section3NumbersFromText() {
    println("§3")
    println("How old are you?")
    val age = readln().toInt()
    println("In 10 years you'll be ${age + 10}.")

    println("Pick a lucky number:")
    val raw = readln()
    val lucky = raw.toIntOrNull()
    if (lucky == null) {
        println("'$raw' is not a number, using 7")
    } else {
        println("Lucky number: $lucky")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Several values on one line
// ───────────────────────────────────────────────────────────────────────────
// Read the whole line, `split` it into pieces, convert each piece.
// `map { it.toInt() }` converts every element (collections — lesson 07).

fun section4SplittingALine() {
    println("§4")
    println("Enter numbers separated by spaces:")
    val numbers = readln().split(" ").map { it.toInt() }
    println("count=${numbers.size} sum=${numbers.sum()} max=${numbers.max()}")

    println("Enter a date like 2026-11-22:")
    val (year, month, day) = readln().split("-").map(String::toInt)   // destructuring
    println("day=$day month=$month year=$year")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Reading until the input ends
// ───────────────────────────────────────────────────────────────────────────
// `readlnOrNull()` returns null when there is nothing more to read (end of
// file / Ctrl+D). `?: break` means "if it's null, leave the loop" (lesson 06).

fun section5ReadUntilTheEnd() {
    println("§5")
    println("Enter colors, one per line (end input to stop):")
    val colors = mutableListOf<String>()
    while (true) {
        val line = readlnOrNull() ?: break
        if (line.isNotBlank()) colors += line.trim()
    }
    println("You entered ${colors.size} colors: ${colors.joinToString()}")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `println`/`print` write to stdout. `readln()` waits for one line of text.
//  • Input is text: convert with toInt() / toIntOrNull() / toDouble()...
//  • `line.split(" ").map { it.toInt() }` for several numbers on one line.
//  • `readlnOrNull() ?: break` to read until the input ends.
// ═══════════════════════════════════════════════════════════════════════════

// Input:
// Ada
// 36
// abc
// 3 4 5
// 2026-11-22
// red
// green

// Output:
// §1
// apples=3 pears=2 total=5
// §2
// What's your name?
// Hello, Ada!
// §3
// How old are you?
// In 10 years you'll be 46.
// Pick a lucky number:
// 'abc' is not a number, using 7
// §4
// Enter numbers separated by spaces:
// count=3 sum=12 max=5
// Enter a date like 2026-11-22:
// day=22 month=11 year=2026
// §5
// Enter colors, one per line (end input to stop):
// You entered 2 colors: red, green
