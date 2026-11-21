/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 01 — Hello, World: the anatomy of a Kotlin program
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : know what every line of a tiny Kotlin program does — packages,
 *         imports, `main`, `println`/`print`, and the three kinds of comments.
 *  Needs: nothing — start here!
 *  Time : ~15 min
 *  Next : lesson 02 — variables (`val` and `var`)
 *  Exercises: exercises/ex01/Ex01.kt
 */

// §1 starts here: the very first line of real code is the PACKAGE.
package lesson01_hello_world

// Imports come right after the package line (explained in §4).
import kotlin.math.PI
import kotlin.math.sqrt as squareRoot

fun main() {
    section1TheSmallestProgram()
    section2PrintAndPrintln()
    section3Comments()
    section4PackagesAndImports()
    section5StatementsAndExpressions()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The smallest program
// ───────────────────────────────────────────────────────────────────────────
// Every Kotlin program starts running in a function called `main`:
//
//     fun main() {
//         println("Hello, World!")
//     }
//
//   • `fun`      — keyword that says "here comes a function".
//   • `main`     — the special name the JVM looks for to start the program.
//   • `()`       — the (empty) list of parameters.
//   • `{ ... }`  — the function body: the instructions, run top to bottom.
//
// 🧒 Like I'm 5:
//    A program is a recipe. `main` is the page that says "START HERE".
//    The computer reads the steps on that page one by one, from top to bottom.
//
// You can also write `fun main(args: Array<String>)` to receive command-line
// arguments (words typed after the program name). Most of the time you don't
// need them, so plain `fun main()` is preferred.
//
// ⚠️ Gotcha: Kotlin is case-sensitive. `Main`, `MAIN` and `main` are three
//    different names — only `main` starts the program. Same for `println`.

fun section1TheSmallestProgram() {
    println("§1")
    println("Hello, World!")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. print vs println
// ───────────────────────────────────────────────────────────────────────────
// `println(x)` prints x and then moves to a NEW LINE ("print line").
// `print(x)` prints x and stays on the SAME line.
// `println()` with nothing inside prints just an empty line break.
// Text in double quotes is a String literal. `\n` inside a string is a newline,
// `\t` is a tab, `\"` is a quote character.

fun section2PrintAndPrintln() {
    println("§2")
    print("Kot")
    print("lin")             // still on the same line → "Kotlin"
    println()                // finish the line
    println("Line 1\nLine 2")
    println("She said \"hi\"")
    println(2 + 3)           // println accepts any value, not only text
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Comments — notes for humans
// ───────────────────────────────────────────────────────────────────────────
// The compiler ignores comments completely. They exist for the people reading.
//
//   // single-line comment: from `//` to the end of the line
//
//   /* block comment:
//      can span many lines */
//
//   /** KDoc comment: documentation for the declaration right below it.
//       IntelliJ shows it when you hover over the name. */
//
// 🧒 Like I'm 5:
//    Comments are sticky notes on the recipe. The cook (computer) skips them;
//    they're there to help the next person who reads the recipe.
//
// 🏗️ In the real world: write comments that explain WHY, not WHAT.
//    `i++ // add one to i` is noise. `// retry because the bank API is flaky` is gold.

/**
 * Greets a person by name.
 *
 * @param name who to greet
 * @return the greeting text
 */
fun greet(name: String): String = "Hi, $name!"

fun section3Comments() {
    println("§3")
    // println("this line is commented out, so it never runs")
    /* println("neither does this one") */
    println(greet("Ada"))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Packages and imports
// ───────────────────────────────────────────────────────────────────────────
// `package lesson01_hello_world` (top of this file) puts everything in this
// file into a named group, so names from different files don't clash. That's
// why every lesson can have its own `main` and its own `section1...` functions.
//
// To use something from ANOTHER package you `import` it:
//
//     import kotlin.math.PI                  // one name
//     import kotlin.math.*                   // every name in the package (use sparingly)
//     import kotlin.math.sqrt as squareRoot  // import and RENAME (fixes name clashes)
//
// Some packages are imported by default, e.g. `kotlin.*`, `kotlin.collections.*`,
// `kotlin.text.*`, `kotlin.io.*` — that's why `println` and `listOf` need no import.
//
// 🧒 Like I'm 5:
//    A package is a folder with a label. An import is saying "I want the toy
//    called PI from the folder labelled kotlin.math". Rename with `as` when two
//    folders have toys with the same name.

fun section4PackagesAndImports() {
    println("§4")
    println("PI = $PI")
    println("sqrt(16) = ${squareRoot(16.0)}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Statements and expressions
// ───────────────────────────────────────────────────────────────────────────
// An EXPRESSION produces a value: `2 + 3`, `"a".uppercase()`, `greet("Bo")`.
// A STATEMENT does something: `println(...)`, `val x = 5`.
// One instruction per line; no semicolons needed (you may use `;` to put two
// on one line, but Kotlin style avoids it).
// In Kotlin, MANY things are expressions — even `if` and `when` (lesson 08).

fun section5StatementsAndExpressions() {
    println("§5")
    val sum = 2 + 3                      // the expression `2 + 3` produces 5
    println("sum = $sum")
    println(if (sum > 4) "big" else "small")   // `if` is an expression too
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Programs start in `fun main()`. Code inside `{ }` runs top to bottom.
//  • `println` ends the line, `print` doesn't.
//  • `//`, `/* */` and `/** KDoc */` are comments; the compiler ignores them.
//  • `package` names the group; `import` brings in names from other packages;
//    `as` renames on import.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Hello, World!
// §2
// Kotlin
// Line 1
// Line 2
// She said "hi"
// 5
// §3
// Hi, Ada!
// §4
// PI = 3.141592653589793
// sqrt(16) = 4.0
// §5
// sum = 5
// big
