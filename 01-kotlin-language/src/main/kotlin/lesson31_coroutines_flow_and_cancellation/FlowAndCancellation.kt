/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 31 — Coroutines, part 2: cancellation, errors, Flow
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : stop coroutines cleanly, handle failures, use timeouts, and work
 *         with streams of values (Flow, StateFlow, Channel).
 *  Needs: lesson 30 (coroutines basics)
 *  Time : ~45 min
 *  Next : lesson 32 — time, ids and encoding
 *  Exercises: exercises/ex31/Ex31.kt
 */
package lesson31_coroutines_flow_and_cancellation

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield

fun main() = runBlocking {
    section1Cancellation()
    section2Timeouts()
    section3Errors()
    section4FlowIsCold()
    section5FlowOperators()
    section6StateFlow()
    section7Channels()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Cancellation is cooperative
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Mum calls "dinner time, stop playing!". A polite kid stops at the next
//    good moment (end of the level). Coroutines are polite kids: `cancel()`
//    only ASKS. The coroutine stops at its next "checkpoint" — every suspend
//    call (delay, await, …) is one. A loop that never suspends never hears mum,
//    unless it checks `ensureActive()` (or `isActive`) itself.
//
// 🏗️ When an HTTP client disconnects, frameworks cancel the request's coroutine.

suspend fun section1Cancellation() = coroutineScope {
    println("§1")
    val job = launch {
        repeat(10) { i ->
            println("  working $i")
            delay(100)                    // checkpoint: cancellation lands here
        }
    }
    delay(250)
    job.cancelAndJoin()                   // ask to stop, then wait until it has stopped
    println("cancelled: ${job.isCancelled}")

    val busy = launch {
        var spins = 0
        while (true) {
            ensureActive()               // pure CPU loop: check manually, or it would never stop
            spins++
            if (spins % 1_000 == 0) yield()   // also give others a turn
        }
    }
    delay(50)
    busy.cancelAndJoin()
    println("busy loop stopped: ${busy.isCancelled}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Timeouts
// ───────────────────────────────────────────────────────────────────────────
// Never wait forever on another service. `withTimeoutOrNull` gives null when
// time is up; `withTimeout` throws TimeoutCancellationException instead.

suspend fun slowService(): String { delay(1_000); return "data" }

suspend fun section2Timeouts() {
    println("§2")
    println(withTimeoutOrNull(100) { slowService() })
    println(withTimeoutOrNull(2_000) { slowService() })
    try {
        withTimeout(100) { slowService() }
    } catch (e: TimeoutCancellationException) {
        println("timed out: ${e.message}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Errors in coroutines
// ───────────────────────────────────────────────────────────────────────────
//  • In `coroutineScope`, one failing child cancels its siblings and the
//    exception comes out of `coroutineScope` — catch it there (or at await()).
//  • In `supervisorScope`, children fail independently: one crash doesn't
//    cancel the others. Unhandled exceptions from `launch` go to a
//    CoroutineExceptionHandler (think: "last-resort error logger").
//
// ⚠️ Gotcha: `catch (e: Exception)` also catches CancellationException. If you
// catch broadly inside a coroutine, rethrow CancellationException, or the
// coroutine "forgets" it was cancelled.

suspend fun section3Errors() {
    println("§3")
    try {
        coroutineScope {
            launch { delay(50); println("  never printed: sibling was cancelled") }
            launch { delay(10); error("payment service down") }
        }
    } catch (e: IllegalStateException) {
        println("coroutineScope failed: ${e.message}")
    }

    val handler = CoroutineExceptionHandler { _, e -> println("  handler logged: ${e.message}") }
    supervisorScope {
        launch(handler) { delay(10); error("email service down") }
        launch { delay(50); println("  other child still finished") }
    }
    println("supervisorScope done")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Flow — a stream of values over time
// ───────────────────────────────────────────────────────────────────────────
// `suspend fun` returns ONE value later. `Flow<T>` returns MANY values over time.
//
// 🧒 Like I'm 5:
//    A Flow is a water TAP, not a bucket. A bucket (List) is already full.
//    A tap gives nothing until someone opens it (`collect`), and each person
//    who opens it gets their own fresh water from the start. That's "cold".

fun countTo(n: Int): Flow<Int> = flow {
    println("  (tap opened)")
    for (i in 1..n) {
        delay(10)
        emit(i)                            // send one value to the collector
    }
}

suspend fun section4FlowIsCold() {
    println("§4")
    val numbers = countTo(3)               // nothing happens yet!
    println("flow created")
    numbers.collect { println("  got $it") }
    println("second collect: ${numbers.toList()}")   // runs the whole flow again
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Flow operators — like collection operators, but lazy
// ───────────────────────────────────────────────────────────────────────────

suspend fun section5FlowOperators() {
    println("§5")
    val result = (1..100).asFlow()
        .filter { it % 2 == 0 }
        .map { it * it }
        .onEach { println("  passing $it") }
        .take(3)                            // stops the upstream after 3 values
        .toList()
    println(result)
    println("first > 50: ${(1..100).asFlow().map { it * it }.first { it > 50 }}")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. StateFlow — "the current value", observable  (+ 🆕 explicit backing fields)
// ───────────────────────────────────────────────────────────────────────────
// StateFlow always HAS a value (`.value`) and tells collectors when it changes.
// Classic pattern: private MUTABLE flow, public READ-ONLY view.
//
// 🆕 Kotlin 2.4 (stable): explicit backing fields. Before, you needed two
// properties (`_count` + `count`). Now ONE property: the public type is
// StateFlow<Int>, but inside the class the field is a MutableStateFlow.

class Counter {
    val count: StateFlow<Int>
        field = MutableStateFlow(0)

    fun increment() {
        count.value++                      // inside the class, `count` is seen as MutableStateFlow
    }
}

suspend fun section6StateFlow() = coroutineScope {
    println("§6")
    val counter = Counter()
    // counter.count.value = 99           // ❌ outside the class it's a read-only StateFlow
    val seen = async(start = CoroutineStart.UNDISPATCHED) { counter.count.take(3).toList() }
    counter.increment(); yield()
    counter.increment(); yield()
    println("collector saw ${seen.await()}, current = ${counter.count.value}")
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Channels — a pipe between two coroutines
// ───────────────────────────────────────────────────────────────────────────
// A Channel is a queue one coroutine `send`s into and another `receive`s from.
// Unlike a Flow, it's HOT: values are produced whether or not anyone listens,
// and each value goes to exactly one receiver. Good for worker queues.

suspend fun section7Channels() = coroutineScope {
    println("§7")
    val jobs = Channel<String>()
    launch {
        for (order in listOf("order-1", "order-2", "order-3")) jobs.send(order)
        jobs.close()                       // no more values: the for-loop below ends
    }
    for (order in jobs) println("  processing $order")
    println("queue drained")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • cancel() asks politely; suspend calls / ensureActive() are the checkpoints.
//  • withTimeout / withTimeoutOrNull protect you from slow services.
//  • coroutineScope: one failure cancels all. supervisorScope: failures are independent.
//  • Flow = cold stream (a tap). Operators are lazy; collect/toList opens the tap.
//  • StateFlow = observable current value; 🆕 `field =` gives a mutable private backing field.
//  • Channel = hot queue between coroutines.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   working 0
//   working 1
//   working 2
// cancelled: true
// busy loop stopped: true
// §2
// null
// data
// timed out: Timed out waiting for 100 ms
// §3
// coroutineScope failed: payment service down
//   handler logged: email service down
//   other child still finished
// supervisorScope done
// §4
// flow created
//   (tap opened)
//   got 1
//   got 2
//   got 3
//   (tap opened)
// second collect: [1, 2, 3]
// §5
//   passing 4
//   passing 16
//   passing 36
// [4, 16, 36]
// first > 50: 64
// §6
// collector saw [0, 1, 2], current = 2
// §7
//   processing order-1
//   processing order-2
//   processing order-3
// queue drained
