/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 12 — Transactions: all or nothing
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : group statements so they succeed or fail TOGETHER, write a reusable
 *         `transaction { }` helper, and see what other connections can see
 *         while a transaction is still running.
 *  Needs: lesson 11 (JDBC), module 01 lessons 10 (exceptions) and 13 (inline)
 *  Time : ~45 min
 *  Next : lesson 13 — the N+1 query problem
 *  Exercises: exercises/ex12/Ex12.kt
 */
package lesson12_transactions

import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.rows
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import kotlin.io.path.createTempDirectory

fun main() {
    section1WhatIsATransaction()
    section2TheProblem()
    section3CommitAndRollback()
    section4ATransactionHelper()
    section5Savepoints()
    section6Isolation()
    section7OneWriterAtATime()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. What is a transaction? ACID
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    You swap trading cards with a friend: you give one card, they give one.
//    It must be BOTH or NEITHER. If you hand yours over and the bell rings
//    before they hand theirs, that's not a swap — that's losing a card.
//    A transaction is a "swap promise": the database does every step, or
//    undoes everything as if nothing happened.
//
// ACID — the four promises of a transaction:
//  A tomicity   all steps happen, or none do.
//  C onsistency rules (constraints) hold before AND after.
//  I solation   others don't see your half-finished work.
//  D urability  once committed, it survives crashes and power cuts.
//
// JDBC starts in *auto-commit* mode: every single statement is its own tiny
// transaction, committed immediately. That's the trap in §2.

fun section1WhatIsATransaction() {
    println("§1")
    memoryDb().use { db -> println("auto-commit by default: ${db.autoCommit}") }
}

fun Connection.createAccounts() = exec(
    """
    CREATE TABLE accounts (
        id            INTEGER PRIMARY KEY,
        owner         TEXT    NOT NULL,
        balance_cents INTEGER NOT NULL CHECK (balance_cents >= 0)
    );
    INSERT INTO accounts (id, owner, balance_cents) VALUES (1, 'ada', 10000), (2, 'bob', 5000);
    """.trimIndent()
)

fun Connection.balances(): String =
    rows("SELECT owner, balance_cents FROM accounts ORDER BY id").joinToString { (owner, cents) -> "$owner=$cents" }

fun Connection.addToBalance(accountId: Int, cents: Int) {
    prepareStatement("UPDATE accounts SET balance_cents = balance_cents + ? WHERE id = ?").use {
        it.setInt(1, cents)
        it.setInt(2, accountId)
        check(it.executeUpdate() == 1) { "account $accountId not found" }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. The problem: two statements, one crash in between
// ───────────────────────────────────────────────────────────────────────────

fun section2TheProblem() {
    println("§2")
    memoryDb().use { db ->
        db.createAccounts()
        println("before:  ${db.balances()}")
        runCatching {
            db.addToBalance(1, -3000)             // take 30.00 from ada… (auto-committed!)
            error("power cut!")                   // …crash before giving it to bob
            @Suppress("UNREACHABLE_CODE")
            db.addToBalance(2, +3000)
        }
        println("after:   ${db.balances()}   ← 30.00 vanished")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. commit() and rollback()
// ───────────────────────────────────────────────────────────────────────────
// Turn auto-commit off → statements pile up in ONE transaction until you
// call commit() (make it permanent) or rollback() (throw it all away).

fun section3CommitAndRollback() {
    println("§3")
    memoryDb().use { db ->
        db.createAccounts()
        db.autoCommit = false
        try {
            db.addToBalance(1, -3000)
            error("power cut!")
        } catch (e: IllegalStateException) {
            db.rollback()                          // undo the half-done transfer
            println("rolled back after: ${e.message}")
        } finally {
            db.autoCommit = true
        }
        println("after:   ${db.balances()}   ← nothing lost")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. A `transaction { }` helper
// ───────────────────────────────────────────────────────────────────────────
// The try/commit/catch/rollback/finally dance is identical every time, so
// wrap it once. `inline` + a lambda (module 01 lesson 13) makes it free.
// Any exception → rollback → rethrow. Normal end → commit.

inline fun <T> Connection.transaction(block: Connection.() -> T): T {
    val previous = autoCommit
    autoCommit = false
    try {
        val result = block()
        commit()
        return result
    } catch (e: Throwable) {
        rollback()
        throw e
    } finally {
        autoCommit = previous
    }
}

fun Connection.transfer(from: Int, to: Int, cents: Int) = transaction {
    addToBalance(from, -cents)
    addToBalance(to, +cents)
}

fun section4ATransactionHelper() {
    println("§4")
    memoryDb().use { db ->
        db.createAccounts()
        db.transfer(from = 1, to = 2, cents = 2500)
        println("ok:      ${db.balances()}")

        // bob only has 75.00 — the CHECK (balance_cents >= 0) constraint fails on the first UPDATE
        val failure = runCatching { db.transfer(from = 2, to = 1, cents = 9900) }.exceptionOrNull()
        println("refused: ${failure?.message?.substringAfter("(")?.removeSuffix(")")}")
        println("after:   ${db.balances()}")

        // destination doesn't exist — the first UPDATE succeeded, then we roll it back
        runCatching { db.transfer(from = 1, to = 99, cents = 100) }
        println("no acct: ${db.balances()}")
    }
}
// 🏗️ In the real world: Spring's @Transactional (module 04) is this helper,
// applied by annotation: `@Transactional fun transfer(...)`. Same rules:
// exception → rollback, normal return → commit.

// ───────────────────────────────────────────────────────────────────────────
// §5. Savepoints: undo PART of a transaction
// ───────────────────────────────────────────────────────────────────────────
// A savepoint is a bookmark inside a transaction. Roll back to it to undo
// only the work after the bookmark; the rest can still be committed.

fun section5Savepoints() {
    println("§5")
    memoryDb().use { db ->
        db.createAccounts()
        db.transaction {
            addToBalance(1, -1000)                        // ada pays 10.00 for a book
            val beforeBonus = setSavepoint("bonus")
            runCatching { addToBalance(2, -999_999) }     // optional bonus step fails…
                .onFailure { rollback(beforeBonus) }      // …undo just that step
            addToBalance(2, +1000)                        // bob receives the 10.00
        }
        println("after:   ${db.balances()}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Isolation: what can OTHER connections see?
// ───────────────────────────────────────────────────────────────────────────
// In-memory databases are private to one connection, so here we use a real
// FILE and open TWO connections to it — like two users of your app.
//
// WAL mode ("write-ahead log") lets readers keep reading the last COMMITTED
// state while someone else is writing. It's what you want for web apps.

fun withTwoConnections(block: (Connection, Connection) -> Unit) {
    val dir = createTempDirectory("lesson12")
    try {
        val url = "jdbc:sqlite:${dir.resolve("bank.db")}"
        DriverManager.getConnection(url).use { a ->
            DriverManager.getConnection(url).use { b ->
                a.exec("PRAGMA journal_mode = WAL")
                a.createAccounts()
                block(a, b)
            }
        }
    } finally {
        dir.toFile().deleteRecursively()
    }
}

fun section6Isolation() {
    println("§6")
    withTwoConnections { a, b ->
        a.autoCommit = false
        a.addToBalance(1, -5000)                          // A: in progress, NOT committed
        println("A sees:  ${a.balances()}")
        println("B sees:  ${b.balances()}   ← A's change is invisible")
        a.commit()
        a.autoCommit = true
        println("B sees:  ${b.balances()}   ← after A commits")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §7. SQLite: one writer at a time
// ───────────────────────────────────────────────────────────────────────────
// SQLite allows MANY readers but only ONE writer at a time (per database
// file). A second writer gets SQLITE_BUSY ("database is locked")… unless it
// is told to wait: `PRAGMA busy_timeout = <ms>`.

fun section7OneWriterAtATime() {
    println("§7")
    withTwoConnections { a, b ->
        b.exec("PRAGMA busy_timeout = 0")                 // B: don't wait at all
        a.autoCommit = false
        a.addToBalance(1, -100)                           // A holds the write lock

        val busy = runCatching { b.addToBalance(2, +100) }.exceptionOrNull() as? SQLException
        println("B without waiting: ${busy?.message?.substringAfter("(")?.removeSuffix(")")}")

        b.exec("PRAGMA busy_timeout = 5000")              // B: wait up to 5 s for the lock
        val committer = Thread {
            Thread.sleep(200)
            a.commit()                                     // A finishes after 200 ms…
        }.apply { start() }
        b.addToBalance(2, +100)                           // …B waits, then writes
        committer.join()
        a.autoCommit = true
        println("B with busy_timeout: waited, then wrote → ${b.balances()}")
    }
}
// ⚠️ Gotcha: a transaction that READS first and WRITES later starts as a
// reader and must "upgrade" to writer — if another writer got in between,
// SQLite fails immediately with SQLITE_BUSY even with busy_timeout. Start
// read-modify-write transactions with `BEGIN IMMEDIATE` (take the write lock
// up front). With sqlite-jdbc: `SQLiteConfig().setTransactionMode(IMMEDIATE)`.
// Module 04 configures this for Spring.

// 🐘 Postgres note:
//  • Many concurrent writers: rows are locked, not the whole database.
//  • MVCC: every transaction sees a snapshot; readers never block writers.
//  • Isolation levels: READ COMMITTED (default) — each statement sees data
//    committed before it started; REPEATABLE READ — the whole transaction
//    sees one snapshot; SERIALIZABLE — as if transactions ran one by one
//    (may abort with a "serialization failure": retry it).
//  • `SELECT … FOR UPDATE` locks the rows you read so nobody changes them
//    before you write (classic for "check balance, then subtract").
//  • SQLite is effectively SERIALIZABLE: only one writer ever runs.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Transaction = all-or-nothing group of statements (ACID).
//  • JDBC auto-commits each statement; set autoCommit = false, then commit/rollback.
//  • Wrap it once: `transaction { }` — Spring's @Transactional does the same.
//  • Savepoints undo part of a transaction.
//  • Others see only COMMITTED data. SQLite: one writer; use WAL + busy_timeout.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// auto-commit by default: true
// §2
// before:  ada=10000, bob=5000
// after:   ada=7000, bob=5000   ← 30.00 vanished
// §3
// rolled back after: power cut!
// after:   ada=10000, bob=5000   ← nothing lost
// §4
// ok:      ada=7500, bob=7500
// refused: CHECK constraint failed: balance_cents >= 0
// after:   ada=7500, bob=7500
// no acct: ada=7500, bob=7500
// §5
// after:   ada=9000, bob=6000
// §6
// A sees:  ada=5000, bob=5000
// B sees:  ada=10000, bob=5000   ← A's change is invisible
// B sees:  ada=5000, bob=5000   ← after A commits
// §7
// B without waiting: database is locked
// B with busy_timeout: waited, then wrote → ada=9900, bob=5100
