/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 14 — Scope functions: let, run, with, apply, also
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : know the five scope functions, how they differ (`this` vs `it`,
 *         what they return), when to pick which, plus takeIf / takeUnless.
 *  Needs: lessons 12–13 (lambdas, lambdas with `this` preview)
 *  Time : ~30 min
 *  Next : lesson 15 — classes and properties
 *  Exercises: exercises/ex14/Ex14.kt
 */
package lesson14_scope_functions

fun main() {
    section1TheIdea()
    section2Let()
    section3Apply()
    section4Also()
    section5RunAndWith()
    section6TakeIf()
    section7ChoosingOne()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The idea
// ───────────────────────────────────────────────────────────────────────────
// A scope function runs a block of code "about" one object. Inside the block
// that object is reachable by a short name: `this` or `it`. They differ in
// only TWO ways:
//
//   function | object inside block | returns              | typical use
//   ---------|---------------------|----------------------|----------------------------
//   let      | it                  | lambda result        | null checks, transform a value
//   run      | this                | lambda result        | compute something from an object
//   with(x)  | this                | lambda result        | "with this object, do…" (not an extension)
//   apply    | this                | the object itself    | configure / build an object
//   also     | it                  | the object itself    | side effects: logging, validation
//
// 🧒 Like I'm 5:
//    You have a teddy bear.
//    apply → "dress the bear" — you get the SAME bear back, now wearing clothes.
//    also  → "take a photo of the bear" — the bear is unchanged, you get it back.
//    let / run / with → "ask the bear a question" — you get the ANSWER back, not the bear.
//    `this` vs `it`: `this` = you're INSIDE the bear costume (just say "nose");
//                    `it`   = you're holding the bear (say "its nose").

data class Request(var url: String = "", var method: String = "GET", val headers: MutableMap<String, String> = mutableMapOf())

fun section1TheIdea() {
    println("§1")
    val name = "kotlin"
    val viaLet = name.let { it.length }           // `it`, returns lambda result
    val viaRun = name.run { length }              // `this` (implicit), returns lambda result
    val viaApply = name.apply { length }          // returns the object itself
    val viaAlso = name.also { it.length }         // returns the object itself
    println("$viaLet $viaRun $viaApply $viaAlso")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `let` — the null-check & transform tool
// ───────────────────────────────────────────────────────────────────────────
// `x?.let { … }` runs the block ONLY if x is not null, with `it` smart-cast to
// non-null. The result can be combined with Elvis `?:` for the null case.

fun findEmail(userId: Int): String? = if (userId == 1) "ada@example.com" else null

fun section2Let() {
    println("§2")
    for (id in listOf(1, 2)) {
        val domain = findEmail(id)?.let { email -> email.substringAfter("@") } ?: "no email"
        println("user $id -> $domain")
    }
    val length = "  padded  ".let { it.trim() }.let { it.length }   // chain transforms
    println(length)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `apply` — configure an object, get it back
// ───────────────────────────────────────────────────────────────────────────
// Inside `apply`, `this` is the object, so you set properties without
// repeating the variable name. Perfect for builders and setup code.

fun section3Apply() {
    println("§3")
    val request = Request().apply {
        url = "https://api.example.com/books"     // this.url
        method = "POST"
        headers["Content-Type"] = "application/json"
    }
    println(request)

    val text = StringBuilder().apply {
        append("Hello")
        append(", ")
        append("world")
    }.toString()
    println(text)
}

// ───────────────────────────────────────────────────────────────────────────
// §4. `also` — "and also do this" (side effects)
// ───────────────────────────────────────────────────────────────────────────
// Returns the same object, so you can slip it into a chain without changing
// the result: logging, debugging, validation, saving a copy.

fun section4Also() {
    println("§4")
    val numbers = mutableListOf(3, 1, 2)
        .also { println("before sort: $it") }
        .apply { sort() }
        .also { println("after sort: $it") }
    println(numbers.sum())

    val user = "ada".also { require(it.isNotBlank()) }   // validate inline
    println(user)
}

// ───────────────────────────────────────────────────────────────────────────
// §5. `run` and `with` — compute a result using an object
// ───────────────────────────────────────────────────────────────────────────
// `x.run { … }` and `with(x) { … }` are the same idea: `this` = x, return the
// lambda result. `run` is an extension (so `?.run` works for null-checks);
// `with` reads naturally when you already have the object and don't chain.
// There's also non-extension `run { … }`: just a block that yields a value.

fun section5RunAndWith() {
    println("§5")
    val request = Request(url = "https://x.dev/a", method = "DELETE")
    val summary = request.run { "$method $url" }
    println(summary)

    val report = with(listOf(4, 8, 15, 16, 23, 42)) {
        "count=$size min=${min()} max=${max()} avg=${average()}"
    }
    println(report)

    val port = run {
        val fromEnv: String? = null                // pretend this came from an env variable
        fromEnv?.toIntOrNull() ?: 8080
    }
    println(port)
}

// ───────────────────────────────────────────────────────────────────────────
// §6. `takeIf` / `takeUnless` — keep the value only if…
// ───────────────────────────────────────────────────────────────────────────
// `x.takeIf { cond }` returns x if cond is true, otherwise null.
// `x.takeUnless { cond }` is the opposite. Combine with `?.let` / `?:`.

fun section6TakeIf() {
    println("§6")
    for (input in listOf("42", "-7", "abc")) {
        val positive = input.toIntOrNull()?.takeIf { it > 0 }
        println("'$input' -> ${positive ?: "rejected"}")
    }
    val name = "   ".takeUnless { it.isBlank() } ?: "anonymous"
    println(name)
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Choosing one — a quick decision guide
// ───────────────────────────────────────────────────────────────────────────
//   Do I want the OBJECT back?
//     yes → am I configuring it?          apply  (this)
//           am I just peeking/logging?    also   (it)
//     no  → is the object maybe null?     ?.let  (it)
//           else                          run / with (this)
//
// ⚠️ Gotcha: don't nest scope functions deeply (`a.let { b.apply { c.also {…} } }`).
// `this`/`it` become ambiguous and the code unreadable. Use named variables instead.

fun section7ChoosingOne() {
    println("§7")
    val maybeUrl: String? = "https://kotlinlang.org"
    maybeUrl
        ?.let { it.removePrefix("https://") }           // transform
        ?.also { println("host: $it") }                  // log
        ?.run { split(".").size }                        // compute
        .let { println("parts: $it") }
}

// 🏗️ In the real world: `apply` builds Spring/HTTP objects (`ResponseEntity`,
// `HttpHeaders().apply { … }`), `?.let` handles optional request params, and
// `also` logs saved entities: `repository.save(book).also { log.info("saved $it") }`.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • let/also use `it`; run/with/apply use `this`.
//  • apply/also return the object; let/run/with return the lambda result.
//  • `?.let { }` = "if not null"; `apply { }` = configure; `also { }` = side effect.
//  • `takeIf`/`takeUnless` turn a value into null when a condition fails.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 6 6 kotlin kotlin
// §2
// user 1 -> example.com
// user 2 -> no email
// 6
// §3
// Request(url=https://api.example.com/books, method=POST, headers={Content-Type=application/json})
// Hello, world
// §4
// before sort: [3, 1, 2]
// after sort: [1, 2, 3]
// 6
// ada
// §5
// DELETE https://x.dev/a
// count=6 min=4 max=42 avg=18.0
// 8080
// §6
// '42' -> 42
// '-7' -> rejected
// 'abc' -> rejected
// anonymous
// §7
// host: kotlinlang.org
// parts: 2
