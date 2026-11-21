/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 34 — Files and I/O
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : create, write, read, list and delete files with `Path` and the
 *         kotlin.io.path extensions; read classpath resources; parse a CSV.
 *  Needs: lessons 07 (collections), 10 (exceptions), 13 (lambdas)
 *  Time : ~30 min
 *  Next : lesson 35 — testing
 *  Exercises: exercises/ex34/Ex34.kt
 */
package lesson34_files_and_io

import java.nio.file.Path
import java.util.Locale
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.appendText
import kotlin.io.path.bufferedWriter
import kotlin.io.path.createDirectories
import kotlin.io.path.createTempDirectory
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.extension
import kotlin.io.path.fileSize
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readLines
import kotlin.io.path.readText
import kotlin.io.path.relativeTo
import kotlin.io.path.useLines
import kotlin.io.path.walk
import kotlin.io.path.writeText

// `deleteRecursively()` is still marked @ExperimentalPathApi in Kotlin 2.4: the API
// may change, so the compiler makes us say "I know" with @OptIn. (Opt-in only affects
// THIS function; nothing else in the module needs it.)
@OptIn(ExperimentalPathApi::class)
fun main() {
    val workDir = createTempDirectory("lesson34")      // a fresh, empty folder just for this run
    try {
        section1Paths()
        section2WriteAndRead(workDir)
        section3BigFiles(workDir)
        section4Directories(workDir)
        section5Resources()
        section6Csv()
    } finally {
        workDir.deleteRecursively()                     // always clean up, even after an error
        println("cleaned up: ${!workDir.exists()}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Path — an address, not a file
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A Path is a house ADDRESS written on paper. Writing the address doesn't
//    build the house, and the house might not exist. Only when you knock
//    (read/write) does the address have to be real.
//
// Use java.nio.file.Path + kotlin.io.path extensions. (The old java.io.File
// still works but Path is the modern API.) Join paths with `/` or `resolve`.

fun section1Paths() {
    println("§1")
    val report = Path("reports") / "2026" / "november.txt"
    println(report.invariantSeparatorsPathString)  // same "/" output on every OS
    println("${report.name} | ${report.nameWithoutExtension} | ${report.extension}")
    println("parent: ${report.parent.invariantSeparatorsPathString}")
    println("exists: ${report.exists()}")           // only an address so far
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Write and read small files
// ───────────────────────────────────────────────────────────────────────────
// For small files, one call does it: writeText / appendText / readText /
// readLines. The default charset is UTF-8.

fun section2WriteAndRead(dir: Path) {
    println("§2")
    val notes = dir / "notes.txt"
    notes.writeText("first line\n")                // creates or OVERWRITES
    notes.appendText("second line\n")              // adds to the end
    println(notes.readLines())
    println("size: ${notes.fileSize()} bytes")
    print(notes.readText())
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Big files: stream them, and always close
// ───────────────────────────────────────────────────────────────────────────
// readLines() loads EVERYTHING into memory — fine for 1 KB, fatal for 5 GB.
// `useLines { }` hands you a lazy Sequence and closes the file afterwards.
//
// 🧒 Like I'm 5:
//    `use { }` is a door that closes itself. Whatever happens inside — even
//    if you trip and fall (an exception) — the door is shut behind you.
//    Open files left "open" eventually crash a server ("too many open files").

fun section3BigFiles(dir: Path) {
    println("§3")
    val log = dir / "app.log"
    log.bufferedWriter().use { out ->               // use = auto-close
        repeat(10_000) { i ->
            out.write(if (i % 1000 == 0) "ERROR request $i failed\n" else "INFO request $i ok\n")
        }
    }
    val errors = log.useLines { lines -> lines.count { it.startsWith("ERROR") } }
    println("error lines: $errors")
    val firstError = log.useLines { lines -> lines.first { it.startsWith("ERROR") } }
    println("first: $firstError")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Directories: create, list, walk
// ───────────────────────────────────────────────────────────────────────────

fun section4Directories(dir: Path) {
    println("§4")
    val src = (dir / "project" / "src").createDirectories()   // creates missing parents too
    (src / "Main.kt").writeText("fun main() {}")
    (src / "Util.kt").writeText("fun util() {}")
    (src / "README.md").writeText("# hi")
    (src / "model").createDirectories()
    (src / "model" / "Book.kt").writeText("class Book")

    println(src.listDirectoryEntries("*.kt").map { it.name }.sorted())   // one level, glob filter
    val project = dir / "project"
    val allKotlin = project.walk()                                       // every file, recursively (lazy)
        .filter { it.extension == "kt" }
        .map { it.relativeTo(project).invariantSeparatorsPathString }
        .sorted()
        .toList()
    println(allKotlin)
    println("model is a directory: ${(src / "model").isDirectory()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Resources — files packed INSIDE your program
// ───────────────────────────────────────────────────────────────────────────
// Files in src/main/resources end up on the classpath (inside the jar). Read
// them with getResourceAsStream, NOT with a Path — inside a jar there is no
// real file on disk.
//
// 🏗️ Spring Boot reads application.yml and Flyway reads db/migration/*.sql
// exactly this way.

fun readResource(name: String): String =
    object {}.javaClass.getResourceAsStream(name)
        ?.bufferedReader()?.use { it.readText() }
        ?: error("resource $name not found")

fun section5Resources() {
    println("§5")
    val csv = readResource("/lesson34_files_and_io/books.csv")
    println(csv.lines().first())
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Parsing a CSV into data classes
// ───────────────────────────────────────────────────────────────────────────

data class Book(val isbn: String, val title: String, val author: String, val price: Double)

fun parseBooks(csv: String): List<Book> =
    csv.lineSequence()
        .drop(1)                                   // skip header
        .filter { it.isNotBlank() }
        .map { line ->
            val (isbn, title, author, price) = line.split(",")
            Book(isbn, title, author, price.toDouble())
        }
        .toList()

fun section6Csv() {
    println("§6")
    val books = parseBooks(readResource("/lesson34_files_and_io/books.csv"))
    books.forEach { println("${it.title.padEnd(17)} ${it.price}") }
    println("total: ${"%.2f".format(Locale.ROOT, books.sumOf { it.price })}")   // ROOT: "." decimal on every machine
}

// ⚠️ Gotcha: real CSV can contain commas inside quotes ("Smith, John").
// `split(",")` breaks on that. For real data use a CSV library.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Path = address; `/` joins, name/extension/parent inspect it.
//  • writeText/appendText/readText/readLines for small files.
//  • Big files: useLines { } (lazy) and bufferedWriter().use { } — `use` always closes.
//  • listDirectoryEntries (one level), walk() (recursive), deleteRecursively() to clean up.
//  • Resources live on the classpath: getResourceAsStream, not Path.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// reports/2026/november.txt
// november.txt | november | txt
// parent: reports/2026
// exists: false
// §2
// [first line, second line]
// size: 23 bytes
// first line
// second line
// §3
// error lines: 10
// first: ERROR request 0 failed
// §4
// [Main.kt, Util.kt]
// [src/Main.kt, src/Util.kt, src/model/Book.kt]
// model is a directory: true
// §5
// isbn,title,author,price
// §6
// Dune              9.99
// Emma              7.5
// Kotlin in Action  44.99
// total: 62.48
// cleaned up: true
