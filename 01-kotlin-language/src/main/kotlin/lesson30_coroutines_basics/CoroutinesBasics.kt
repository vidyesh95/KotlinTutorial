/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 30 — Coroutines, part 1: doing many things while waiting
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : understand `suspend`, `runBlocking`, `launch`, `async/await`,
 *         structured concurrency and dispatchers.
 *  Needs: lessons 11–13 (functions, lambdas, higher-order functions)
 *  Time : ~40 min
 *  Next : lesson 31 — cancellation, errors and Flow
 *  Exercises: exercises/ex30/Ex30.kt
 *
 *  Library: kotlinx-coroutines-core (already a dependency of this module).
 */
package lesson30_coroutines_basics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.measureTime

fun main() = runBlocking {
    section1WhyCoroutines()
    section2SuspendFunctions()
    section3Launch()
    section4AsyncAwait()
    section5StructuredConcurrency()
    section6Dispatchers()
    section7Jobs()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Why coroutines?
// ───────────────────────────────────────────────────────────────────────────
// A backend spends most of its time WAITING: for the database, for another
// HTTP service, for a file. A thread that just waits is expensive (each one
// needs ~1 MB of memory; a server can have a few thousand at most).
//
// 🧒 Like I'm 5:
//    One chef, many dishes. The chef puts a cake in the oven (40 min wait).
//    A bad chef stands staring at the oven. A good chef starts the soup,
//    chops vegetables, and comes back when the oven beeps.
//    Coroutines make your code the GOOD chef: while one task waits, the same
//    thread works on another task. Coroutines are the dishes, the thread is the chef.
//
// Coroutines are so cheap you can start 100 000 of them without trouble.

suspend fun section1WhyCoroutines() = coroutineScope {
    println("§1")
    var finished = 0
    val time = measureTime {
        coroutineScope {
            repeat(100_000) {
                launch {
                    delay(100)          // 100 000 tasks each waiting 100 ms…
                    finished++
                }
            }
        }
    }
    println("finished: $finished")
    println("took well under 100 000 × 100 ms: ${time < 10_000.milliseconds}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `suspend` functions — the pause button
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A `suspend` function has a PAUSE BUTTON. When it waits (delay, DB call),
//    it presses pause, gives the thread back to other work, and later resumes
//    exactly where it stopped — local variables and all.
//
// Rules:
//  • A suspend function can only be called from another suspend function or
//    from a coroutine (launch / async / runBlocking).
//  • `delay(ms)` = suspending sleep. It pauses THIS coroutine, not the thread.
//  • `runBlocking { }` bridges normal code → coroutine world. It BLOCKS the
//    current thread until done. Use it in `main` and tests, never inside a
//    server request handler.

suspend fun fetchUserName(id: Int): String {
    delay(50)                           // pretend this is a database call
    return "user-$id"
}

suspend fun section2SuspendFunctions() {
    println("§2")
    val name = fetchUserName(7)         // reads like normal sequential code!
    println(name)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `launch` — start a task, don't need a result
// ───────────────────────────────────────────────────────────────────────────
// `launch` starts a new coroutine and returns a `Job` immediately. The code
// after it keeps running. Use it for "fire and forget" work (send an email).

suspend fun section3Launch() = coroutineScope {
    println("§3")
    launch {
        delay(100)
        println("email sent")
    }
    launch {
        delay(50)
        println("audit log written")
    }
    println("request answered")        // printed first: launch didn't wait
}

// ───────────────────────────────────────────────────────────────────────────
// §4. `async` / `await` — start tasks, collect results
// ───────────────────────────────────────────────────────────────────────────
// `async` is `launch` that returns a value later: a `Deferred<T>`. Call
// `.await()` to get it (suspends until ready). Start several asyncs first,
// THEN await — that's what makes them run at the same time.

suspend fun loadPrice(): Int { delay(100); return 20 }
suspend fun loadStock(): Int { delay(100); return 3 }
suspend fun loadRating(): Double { delay(100); return 4.5 }

suspend fun section4AsyncAwait() = coroutineScope {
    println("§4")
    val sequential = measureTime {
        val total = loadPrice() * loadStock()         // one after another: ~200 ms
        println("sequential total = $total")
    }
    val concurrent = measureTime {
        val price = async { loadPrice() }             // both start now…
        val stock = async { loadStock() }
        val rating = async { loadRating() }
        println("concurrent total = ${price.await() * stock.await()}, rating ${rating.await()}")  // …~100 ms
    }
    println("concurrent was faster: ${concurrent < sequential}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Structured concurrency — no task gets lost
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A mother duck doesn't leave the pond until ALL her ducklings are out.
//    A coroutine scope is the mother duck: it waits for every child coroutine
//    it started. If one duckling gets hurt (exception), the whole family stops.
//
// `coroutineScope { }` creates such a scope. It returns only when all children
// are done. So you can never "forget" a background task by accident.

suspend fun section5StructuredConcurrency() {
    println("§5")
    coroutineScope {
        launch { delay(30); println("  child A done") }
        launch { delay(10); println("  child B done") }
        println("  scope body finished, waiting for children…")
    }
    println("scope returned after ALL children")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Dispatchers — which threads run the coroutine
// ───────────────────────────────────────────────────────────────────────────
//  • Dispatchers.Default — a few threads (= CPU cores) for heavy computation.
//  • Dispatchers.IO      — many threads for BLOCKING calls (JDBC, files, old libraries).
//  • withContext(d) { } — run this block on dispatcher d, then come back.
//
// 🏗️ In the real world: JDBC (modules 03–05) is blocking, so database calls
// from coroutines are wrapped in `withContext(Dispatchers.IO) { ... }`.

suspend fun sumOfSquares(n: Int): Long = withContext(Dispatchers.Default) {
    (1..n).sumOf { it.toLong() * it }               // CPU work off the caller's thread
}

suspend fun readConfigBlocking(): String = withContext(Dispatchers.IO) {
    Thread.sleep(20)                               // a BLOCKING call is OK on IO
    "port=8080"
}

suspend fun section6Dispatchers() {
    println("§6")
    println(sumOfSquares(1_000))
    println(readConfigBlocking())
}

// ⚠️ Gotcha: `Thread.sleep()` (or any blocking call) inside a coroutine on a
// non-IO dispatcher freezes the THREAD — every other coroutine sharing it stops
// too. Use `delay()` for waiting, and `Dispatchers.IO` for unavoidable blocking.

// ───────────────────────────────────────────────────────────────────────────
// §7. Job — a handle to a running coroutine
// ───────────────────────────────────────────────────────────────────────────

suspend fun section7Jobs() = coroutineScope {
    println("§7")
    val job: Job = launch { delay(50) }
    println("active=${job.isActive}, completed=${job.isCompleted}")
    job.join()                                    // suspend until it finishes
    println("active=${job.isActive}, completed=${job.isCompleted}")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `suspend fun` can pause without blocking the thread; call it from coroutines.
//  • runBlocking = bridge from normal code (main/tests). launch = no result. async/await = result.
//  • Start all asyncs before awaiting to run them concurrently.
//  • Scopes wait for their children (structured concurrency) — nothing leaks.
//  • Default for CPU work, IO for blocking calls, switch with withContext.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// finished: 100000
// took well under 100 000 × 100 ms: true
// §2
// user-7
// §3
// request answered
// audit log written
// email sent
// §4
// sequential total = 60
// concurrent total = 60, rating 4.5
// concurrent was faster: true
// §5
//   scope body finished, waiting for children…
//   child B done
//   child A done
// scope returned after ALL children
// §6
// 333833500
// port=8080
// §7
// active=true, completed=false
// active=false, completed=true
