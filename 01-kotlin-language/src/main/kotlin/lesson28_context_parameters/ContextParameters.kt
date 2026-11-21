/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 28 — Context parameters: helpers that are "always in the room"
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : pass "ambient" things (logger, current user, transaction) to many
 *         functions WITHOUT adding a parameter to every single call.
 *  Needs: lessons 11–14 (functions, lambdas, scope functions), 29 helps but isn't required
 *  Time : ~30 min
 *  Next : lesson 29 — lambdas with receiver and DSLs
 *  Exercises: exercises/ex28/Ex28.kt
 *
 *  🆕 Kotlin 2.x: context parameters arrived as a preview in 2.2 and are
 *  STABLE since Kotlin 2.4 — no compiler flag needed.
 */
package lesson28_context_parameters

fun main() {
    section1TheProblem()
    section2DeclaringContext()
    section3ProvidingContext()
    section4SeveralContexts()
    section5AnonymousContext()
    section6ContextProperties()
    section7Transactions()
}

interface Logger {
    fun log(message: String)
}

class ConsoleLogger(private val prefix: String) : Logger {
    override fun log(message: String) = println("[$prefix] $message")
}

data class CurrentUser(val name: String, val isAdmin: Boolean)

// ───────────────────────────────────────────────────────────────────────────
// §1. The problem: "parameter drilling"
// ───────────────────────────────────────────────────────────────────────────
// Many functions need the same helper: a logger, the logged-in user, the open
// database transaction. With normal parameters we must hand it down through
// EVERY layer, even layers that don't use it themselves — they just pass it on.

fun registerBookOld(title: String, logger: Logger) {
    logger.log("registering $title")
    saveBookOld(title, logger)                 // pass it on…
}

fun saveBookOld(title: String, logger: Logger) {
    logger.log("saved $title")                 // …only to use it here
}

fun section1TheProblem() {
    println("§1")
    registerBookOld("Dune", ConsoleLogger("old"))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Declaring a context parameter
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    In a classroom the teacher is ALWAYS in the room. Kids don't carry the
//    teacher from desk to desk — anyone who needs help just asks "teacher!".
//    A context parameter is the teacher: you set it up once around a block of
//    code, and every function inside that needs it can simply use it.
//
// Syntax: `context(name: Type)` goes BEFORE `fun`. Inside, use `name` like a
// normal parameter. Calling another function that needs the same context
// passes it along automatically.

context(logger: Logger)
fun registerBook(title: String) {
    logger.log("registering $title")
    saveBook(title)                            // no logger argument — it flows along by itself
}

context(logger: Logger)
fun saveBook(title: String) {
    logger.log("saved $title")
}

fun section2DeclaringContext() {
    println("§2")
    with(ConsoleLogger("new")) {               // a Logger is now "in the room"
        registerBook("Dune")
    }
    // registerBook("Dune")                    // ❌ compile error: No context argument for 'logger: Logger' found.
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Providing a context: `with(...)` or the stdlib `context(...)` function
// ───────────────────────────────────────────────────────────────────────────
// Any value that is an implicit receiver (`this`) can satisfy a context:
//   • `with(x) { ... }` — scope function from lesson 14
//   • `context(x) { ... }` — a stdlib function made exactly for this; it reads
//     nicer and accepts several values at once (§4).
// Inside a function that ALREADY has the context, nothing is needed (§2).

fun section3ProvidingContext() {
    println("§3")
    context(ConsoleLogger("ctx")) {
        registerBook("Emma")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Several contexts at once
// ───────────────────────────────────────────────────────────────────────────

context(logger: Logger, user: CurrentUser)
fun deleteBook(title: String): Boolean {
    if (!user.isAdmin) {
        logger.log("${user.name} may NOT delete $title")
        return false
    }
    logger.log("${user.name} deleted $title")
    return true
}

fun section4SeveralContexts() {
    println("§4")
    val logger = ConsoleLogger("auth")
    context(logger, CurrentUser("Ada", isAdmin = true)) {
        println(deleteBook("Dune"))
    }
    context(logger, CurrentUser("Bob", isAdmin = false)) {
        println(deleteBook("Dune"))
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Anonymous context: `_` and `contextOf<T>()`
// ───────────────────────────────────────────────────────────────────────────
// If your function only needs the context to PASS IT ON, you don't have to
// name it — write `_`. If you later need the value anyway, `contextOf<T>()`
// fetches it.

context(_: Logger)
fun importBooks(titles: List<String>) {
    titles.forEach { saveBook(it) }            // forwarded implicitly
    contextOf<Logger>().log("imported ${titles.size} books")
}

fun section5AnonymousContext() {
    println("§5")
    context(ConsoleLogger("import")) {
        importBooks(listOf("A", "B"))
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Context properties
// ───────────────────────────────────────────────────────────────────────────
// Properties can have contexts too — but only computed ones (a getter, no
// stored value), because there is no single object to store the value in.

context(user: CurrentUser)
val greeting: String
    get() = if (user.isAdmin) "Welcome back, boss ${user.name}" else "Hi, ${user.name}"

fun section6ContextProperties() {
    println("§6")
    context(CurrentUser("Ada", isAdmin = true)) { println(greeting) }
    context(CurrentUser("Bob", isAdmin = false)) { println(greeting) }
}

// ───────────────────────────────────────────────────────────────────────────
// §7. The real-world shape: a transaction scope
// ───────────────────────────────────────────────────────────────────────────
// A lambda type can require a context too: `context(Transaction) () -> R`.
// `inTransaction` opens the transaction, runs your block with it "in the room",
// then commits. Every repository function inside joins the SAME transaction
// without anybody passing it around.

class Transaction(val id: Int) {
    fun execute(sql: String) = println("  tx$id: $sql")
}

fun <R> inTransaction(id: Int, block: context(Transaction) () -> R): R {
    val tx = Transaction(id)
    println("BEGIN tx$id")
    val result = context(tx) { block() }
    println("COMMIT tx$id")
    return result
}

context(tx: Transaction)
fun insertBook(title: String) = tx.execute("INSERT INTO book(title) VALUES ('$title')")

fun section7Transactions() {
    println("§7")
    val inserted = inTransaction(id = 1) {
        insertBook("Dune")
        insertBook("Emma")
        2
    }
    println("inserted $inserted rows")
}

// 🏗️ In the real world: Spring keeps the current transaction and the logged-in
// user in hidden thread-local "holders" (TransactionSynchronizationManager,
// SecurityContextHolder). Context parameters do the same job but VISIBLY and
// checked by the compiler. Module 02 (Dependency Injection) builds on this.
//
// ⚠️ Gotcha: don't turn every dependency into a context parameter. Services a
// class always needs (a repository, an HTTP client) belong in its CONSTRUCTOR
// (constructor injection, module 02). Context parameters suit "ambient" things
// that belong to the current call: transaction, current user, logger, locale.
//
// ⚠️ Passing a context explicitly by name (`registerBook("x", logger = l)`) is
// still Experimental (`-Xexplicit-context-arguments`); this tutorial doesn't use it.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `context(name: Type) fun f()` — f can only be called where a Type is "in the room".
//  • Provide it with `context(value) { ... }` or `with(value) { ... }`.
//  • Inside a context function, calls to other context functions get it for free.
//  • `_` = unnamed context, `contextOf<T>()` = fetch it anyway.
//  • Use for ambient per-call things (tx, user, logger), not for core service dependencies.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// [old] registering Dune
// [old] saved Dune
// §2
// [new] registering Dune
// [new] saved Dune
// §3
// [ctx] registering Emma
// [ctx] saved Emma
// §4
// [auth] Ada deleted Dune
// true
// [auth] Bob may NOT delete Dune
// false
// §5
// [import] saved A
// [import] saved B
// [import] imported 2 books
// §6
// Welcome back, boss Ada
// Hi, Bob
// §7
// BEGIN tx1
//   tx1: INSERT INTO book(title) VALUES ('Dune')
//   tx1: INSERT INTO book(title) VALUES ('Emma')
// COMMIT tx1
// inserted 2 rows
