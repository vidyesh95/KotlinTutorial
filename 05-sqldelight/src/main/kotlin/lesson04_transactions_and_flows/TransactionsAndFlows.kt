/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQLDelight Lesson 04 — Transactions and reactive queries (Flow)
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : group writes with SQLDelight's own `transaction {}` (outside Spring), and
 *         turn a query into a Flow that re-emits whenever its tables change.
 *  Needs: lesson 02; module 01 lessons 30–31 (coroutines, Flow); module 03 lesson 12
 *  Time : ~35 min
 *  Next : lessons/05-spring-integration.md
 *  Exercises: exercises/ex04
 */
package lesson04_transactions_and_flows

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import bookstore.db.BookstoreDatabase
import bookstore.db.inMemoryBookstore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    section1Commit()
    section2ExceptionRollsBack()
    section3ExplicitRollback()
    section4TransactionWithResult()
    section5Callbacks()
    section6QueriesAsFlows()
}

fun BookstoreDatabase.stockOf(id: Long): Int = booksQueries.selectById(id).executeAsOne().stock

// ───────────────────────────────────────────────────────────────────────────
// §1. `transaction {}` — all writes inside succeed or fail together
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5: the shop till again (module 04, lesson 06). Scan everything, then pay.
// If anything goes wrong, the whole receipt is cancelled.
//
// SQLDelight's Transacter (every BookstoreDatabase is one) gives you:
//   transaction { … }               → Unit
//   transactionWithResult { … }     → returns a value
// The block ends normally → COMMIT. It throws → ROLLBACK, and the exception is rethrown.

fun section1Commit() {
    println("§1")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        db.transaction {
            db.booksQueries.takeStock(quantity = 2, id = 1)
            db.booksQueries.takeStock(quantity = 1, id = 2)
        }
        println("Dune ${db.stockOf(1)}, Dune Messiah ${db.stockOf(2)}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. An exception rolls everything back
// ───────────────────────────────────────────────────────────────────────────

fun section2ExceptionRollsBack() {
    println("§2")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val result = runCatching {
            db.transaction {
                db.booksQueries.takeStock(quantity = 2, id = 1)                  // works…
                val taken = db.booksQueries.takeStock(quantity = 1, id = 4).value // Emma: stock 0
                if (taken == 0L) error("Emma is out of stock")                   // …so undo everything
            }
        }
        println("failed with: ${result.exceptionOrNull()?.message}")
        println("Dune still ${db.stockOf(1)}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. rollback() — undo without an exception escaping
// ───────────────────────────────────────────────────────────────────────────
// Inside the block, `rollback()` stops it immediately and undoes the writes.
// No exception reaches the caller.

fun section3ExplicitRollback() {
    println("§3")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        db.transaction {
            db.booksQueries.updatePrice(priceCents = 1, id = 1)
            println("inside: Dune costs ${db.booksQueries.selectById(1).executeAsOne().price_cents}")
            rollback()
            @Suppress("UNREACHABLE_CODE")
            println("never printed")
        }
        println("after:  Dune costs ${db.booksQueries.selectById(1).executeAsOne().price_cents}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. transactionWithResult — return a value (or a fallback when rolling back)
// ───────────────────────────────────────────────────────────────────────────

fun section4TransactionWithResult() {
    println("§4")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        fun buy(bookId: Long, quantity: Int): String = db.transactionWithResult {
            val taken = db.booksQueries.takeStock(quantity, bookId).value
            if (taken == 0L) rollback("not enough stock for book $bookId")   // rollback(value) = result
            "bought $quantity of book $bookId, ${db.stockOf(bookId)} left"
        }
        println(buy(bookId = 2, quantity = 1))
        println(buy(bookId = 2, quantity = 5))
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. afterCommit / afterRollback — side effects only when the outcome is known
// ───────────────────────────────────────────────────────────────────────────
// Sending a confirmation e-mail INSIDE the transaction is a classic bug: the e-mail goes
// out, then the transaction rolls back, and the customer got a lie. Register the side
// effect to run AFTER the commit instead.
// 🏗️ In Spring: @TransactionalEventListener(phase = AFTER_COMMIT) does the same job.

fun section5Callbacks() {
    println("§5")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        db.transaction {
            afterCommit { println("  committed → send confirmation e-mail") }
            afterRollback { println("  rolled back → no e-mail") }
            db.booksQueries.takeStock(quantity = 1, id = 3)
        }
        db.transaction {
            afterCommit { println("  committed → send confirmation e-mail") }
            afterRollback { println("  rolled back → no e-mail") }
            rollback()
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Queries as Flows: "tell me whenever the answer changes"
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5: instead of asking "is there low stock?" every minute, you ask once and
// the database taps you on the shoulder each time the books table changes.
//
//   db.booksQueries.lowStock(2)     a Query
//     .asFlow()                     Flow<Query> — emits now, and again after every change to `books`
//     .mapToList(Dispatchers.IO)    Flow<List<LowStock>> — runs the query off the main thread
//
// SQLDelight knows each query's tables, and every generated write notifies the tables it
// touched. ⚠️ It re-runs on ANY change to those tables — even one that doesn't change this
// query's result (see the 3rd emission below). Use `.distinctUntilChanged()` if that matters.
// (To keep the output deterministic we wait for each emission before the next write.)

fun section6QueriesAsFlows() = runBlocking {
    println("§6")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val emissions = Channel<List<String>>()
        val watcher = launch {
            db.booksQueries.lowStock(threshold = 2).asFlow()
                .mapToList(Dispatchers.IO)
                .map { rows -> rows.map { "${it.title} (${it.stock})" } }
                .take(3)
                .collect { emissions.send(it) }
        }
        println("1st: ${emissions.receive()}")
        db.booksQueries.takeStock(quantity = 10, id = 1)          // Dune 12 → 2: now low stock
        println("2nd: ${emissions.receive()}")
        db.booksQueries.updatePrice(priceCents = 1499, id = 3)    // touches `books`, result unchanged
        println("3rd: ${emissions.receive()}")
        watcher.join()
    }
}
// 🏗️ In the real world: Android/Compose UIs collect such Flows to redraw automatically.
// On a server you'd more often push changes with events or websockets.
//
// ⚠️ In a Spring app, DON'T use `database.transaction {}` — use @Transactional (lesson 05).
// Two transaction managers on one connection would trip over each other.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • transaction {} / transactionWithResult {}: commit on success, rollback on exception.
//  • rollback() / rollback(value) undo without an exception escaping.
//  • afterCommit / afterRollback for side effects that depend on the outcome.
//  • query.asFlow().mapToList(dispatcher) = a live query that re-emits on table changes.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Dune 10, Dune Messiah 1
// §2
// failed with: Emma is out of stock
// Dune still 12
// §3
// inside: Dune costs 1
// after:  Dune costs 1299
// §4
// bought 1 of book 2, 1 left
// not enough stock for book 2
// §5
//   committed → send confirmation e-mail
//   rolled back → no e-mail
// §6
// 1st: [Emma (0), Dune Messiah (2)]
// 2nd: [Emma (0), Dune (2), Dune Messiah (2)]
// 3rd: [Emma (0), Dune (2), Dune Messiah (2)]
