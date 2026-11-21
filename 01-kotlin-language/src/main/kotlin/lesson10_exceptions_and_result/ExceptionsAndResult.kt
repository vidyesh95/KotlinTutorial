/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 10 — When things go wrong: exceptions and Result
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : throw and catch exceptions, use `try` as an expression, understand
 *         `Nothing`, validate with require/check/error, close resources with
 *         `use`, and model failure as a value with `Result` / `runCatching`.
 *  Needs: lessons 08–09
 *  Time : ~35 min
 *  Next : lesson 11 — functions
 *  Exercises: exercises/ex10/Ex10.kt
 */
package lesson10_exceptions_and_result

import java.io.Closeable

fun main() {
    section1ThrowAndCatch()
    section2TryIsAnExpression()
    section3Nothing()
    section4RequireCheckError()
    section5CustomExceptions()
    section6Use()
    section7Result()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Throwing and catching
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    An exception is a fire alarm. Somewhere deep in the building (a function)
//    something goes wrong and the alarm rings (`throw`). Everybody stops what
//    they're doing and the alarm travels UP floor by floor (caller by caller)
//    until someone whose job it is handles it (`catch`). If nobody does, the
//    whole building is evacuated (the program crashes with a stack trace).
//
//   try     { risky code }
//   catch   (e: SomeException) { handle it }   ← first matching type wins
//   finally { always runs: success, failure, or even return }
//
// Kotlin has NO checked exceptions: you never have to declare `throws`.

fun section1ThrowAndCatch() {
    println("§1")
    val names = listOf("Striker")
    try {
        println(names[1])                          // index 1 doesn't exist
        println("never printed")
    } catch (e: IndexOutOfBoundsException) {
        println("caught: ${e::class.simpleName}")
    } finally {
        println("finally always runs")
    }

    try {
        println(10 / 0)
    } catch (e: ArithmeticException) {
        println("caught: ${e.message}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `try` is an expression
// ───────────────────────────────────────────────────────────────────────────
// Like `if`/`when`, `try` returns a value: the last expression of the `try`
// block, or of the `catch` block that ran. (`finally` does NOT affect the value.)

fun parsePort(text: String): Int = try {
    text.toInt()
} catch (e: NumberFormatException) {
    8080                                           // fallback value
}

fun section2TryIsAnExpression() {
    println("§2")
    println(parsePort("9000"))
    println(parsePort("nine thousand"))
    println("abc".toIntOrNull() ?: -1)              // often an *OrNull function is simpler than try
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `throw` is an expression of type `Nothing`
// ───────────────────────────────────────────────────────────────────────────
// `Nothing` is the type of "this never finishes normally". Because Nothing is
// a subtype of EVERY type, `throw` fits anywhere a value is expected — e.g. on
// the right of the Elvis operator `?:`.

fun fail(message: String): Nothing = throw IllegalStateException(message)

fun findUser(id: Int): String? = if (id == 1) "ada" else null

fun section3Nothing() {
    println("§3")
    val name: String = findUser(1) ?: fail("no user")   // compiler knows: after this, name is not null
    println(name)
    try {
        findUser(2) ?: throw NoSuchElementException("user 2 not found")
    } catch (e: NoSuchElementException) {
        println(e.message)
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. require / check / error — one-line guards
// ───────────────────────────────────────────────────────────────────────────
//   require(cond) { msg } → IllegalArgumentException  ("the CALLER gave me bad input")
//   check(cond)   { msg } → IllegalStateException     ("I am in a bad state")
//   error(msg)            → IllegalStateException, always (returns Nothing)
//   requireNotNull(x) / checkNotNull(x) → also smart-cast x to non-null

class BankAccount(private var balance: Int) {
    private var closed = false

    fun withdraw(amount: Int): Int {
        require(amount > 0) { "amount must be positive, was $amount" }
        check(!closed) { "account is closed" }
        if (amount > balance) error("insufficient funds: $balance < $amount")
        balance -= amount
        return balance
    }

    fun close() { closed = true }
}

fun section4RequireCheckError() {
    println("§4")
    val account = BankAccount(100)
    println(account.withdraw(30))
    for (attempt in listOf<() -> Unit>(
        { account.withdraw(-5) },
        { account.withdraw(1000) },
        { account.close(); account.withdraw(1) },
    )) {
        try {
            attempt()
        } catch (e: IllegalArgumentException) {
            println("bad argument: ${e.message}")
        } catch (e: IllegalStateException) {
            println("bad state: ${e.message}")
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Your own exception types
// ───────────────────────────────────────────────────────────────────────────
// Extend `Exception` (or a more specific one like IllegalArgumentException)
// and PASS the message to the parent constructor — otherwise `e.message` is null.

class IllegalIdException(message: String) : Exception(message)

fun checkAge(name: String, age: Int): String {
    if (age < 18) throw IllegalIdException("$name is younger than 18")
    return "$name passed"
}

fun section5CustomExceptions() {
    println("§5")
    for ((name, age) in listOf("ada" to 36, "abra" to 17)) {
        try {
            println(checkAge(name, age))
        } catch (e: IllegalIdException) {
            println("rejected: ${e.message}")
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. `use { }` — always close what you open
// ───────────────────────────────────────────────────────────────────────────
// Files, DB connections, network sockets must be closed even when an
// exception happens. `resource.use { … }` calls `close()` for you, always.
// (Java's try-with-resources.)

class FakeConnection(private val name: String) : Closeable {
    fun query(sql: String): String = if ("DROP" in sql) throw IllegalArgumentException("refused") else "rows from $name"
    override fun close() = println("closed $name")
}

fun section6Use() {
    println("§6")
    val result = FakeConnection("db1").use { it.query("SELECT 1") }
    println(result)
    try {
        FakeConnection("db2").use { it.query("DROP TABLE users") }
    } catch (e: IllegalArgumentException) {
        println("query failed: ${e.message}")      // note: "closed db2" printed BEFORE this
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §7. `Result` — failure as a normal value
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Instead of ringing the fire alarm, you hand back a lunchbox with a note
//    inside: either "here's your sandwich 🥪" (success) or "sorry, no bread"
//    (failure). The caller opens it and decides calmly what to do.
//
// `runCatching { … }` runs code and wraps the outcome in `Result<T>`:
//   .getOrNull() / .getOrElse { } / .getOrDefault(x) / .map { } / .onSuccess / .onFailure / .exceptionOrNull()

fun section7Result() {
    println("§7")
    val ok: Result<Int> = runCatching { "42".toInt() }
    val bad: Result<Int> = runCatching { "4x2".toInt() }

    println(ok.map { it * 2 }.getOrNull())
    println(bad.map { it * 2 }.getOrNull())
    println(bad.getOrElse { e -> -1.also { println("recovering from ${e::class.simpleName}") } })
    bad.onFailure { println("failure: ${it.message}") }
        .onSuccess { println("never printed") }
    println("isSuccess=${ok.isSuccess}, isFailure=${bad.isFailure}")
}

// ⚠️ Gotcha: never `catch (e: Exception) {}` and do nothing ("swallowing").
// The bug is still there — you just can't see it any more. At minimum log it.

// 🏗️ In the real world: which to use?
//   • Expected "no value" (user not found)   → return null (`User?`)
//   • Expected failure with a reason          → sealed result type (lesson 18) or Result
//   • Programming bug / broken invariant      → throw (require/check)
// Spring turns uncaught exceptions into HTTP errors; in lesson 04 of the Spring
// module you'll map e.g. `NotFoundException` → 404 with `@ExceptionHandler`.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `throw` raises, `try/catch/finally` handles; Kotlin has no checked exceptions.
//  • `try` is an expression; `throw` has type `Nothing` so it fits after `?:`.
//  • require → bad argument, check → bad state, error → always throws.
//  • `use { }` closes resources; `runCatching` turns exceptions into `Result`.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// caught: IndexOutOfBoundsException
// finally always runs
// caught: / by zero
// §2
// 9000
// 8080
// -1
// §3
// ada
// user 2 not found
// §4
// 70
// bad argument: amount must be positive, was -5
// bad state: insufficient funds: 70 < 1000
// bad state: account is closed
// §5
// ada passed
// rejected: abra is younger than 18
// §6
// closed db1
// rows from db1
// closed db2
// query failed: refused
// §7
// 84
// null
// recovering from NumberFormatException
// -1
// failure: For input string: "4x2"
// isSuccess=true, isFailure=true
