/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 29 — Lambdas with receiver and DSLs
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : understand `T.() -> Unit`, see how `apply`/`buildString` work inside,
 *         and build two small DSLs (routes + SQL query builder).
 *  Needs: lessons 12–14 (lambdas, higher-order functions, scope functions), 20 (infix)
 *  Time : ~35 min
 *  Next : lesson 30 — coroutines basics
 *  Exercises: exercises/ex29/Ex29.kt
 */
package lesson29_dsls_and_receivers

fun main() {
    section1LambdaWithReceiver()
    section2HowApplyWorks()
    section3RouteDsl()
    section4QueryDsl()
    section5DslMarker()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. A lambda WITH a receiver
// ───────────────────────────────────────────────────────────────────────────
// Normal lambda type:          (String) -> String   — the string comes in as `it`
// Lambda with receiver type:   String.() -> String  — the string becomes `this`
//
// 🧒 Like I'm 5:
//    A normal lambda is a robot you HAND a toy to: "here, paint this (it)".
//    A lambda with receiver is a robot you put INSIDE the toy house: it can
//    just say "paint the door", "open the window" — everything in that house
//    is directly reachable, no need to say whose door.
//
// Inside the lambda, `this` is the receiver, so you can call its members
// without a prefix — exactly like code inside a class or an extension function.

val shout: String.() -> String = { uppercase() + "!" }     // uppercase() means this.uppercase()
val shoutNormal: (String) -> String = { it.uppercase() + "!" }

fun section1LambdaWithReceiver() {
    println("§1")
    println("hello".shout())        // call like an extension function…
    println(shout("hi"))            // …or like a normal function (receiver = first argument)
    println(shoutNormal("hey"))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. How `apply` and `buildString` work — no magic
// ───────────────────────────────────────────────────────────────────────────
// You used them in lesson 14. Here are tiny re-implementations. The trick is
// ALWAYS the same: create an object, run the user's lambda WITH that object as
// receiver, then return something.

inline fun <T> T.myApply(block: T.() -> Unit): T {
    this.block()                    // run block with `this` as receiver
    return this
}

inline fun myBuildString(block: StringBuilder.() -> Unit): String =
    StringBuilder().myApply(block).toString()

fun section2HowApplyWorks() {
    println("§2")
    val list = mutableListOf(1).myApply { add(2); add(3) }   // add() = this.add()
    println(list)
    val text = myBuildString {
        append("Kotlin ")
        append(2.4)
    }
    println(text)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Your first DSL: routes
// ───────────────────────────────────────────────────────────────────────────
// DSL = Domain-Specific Language: code that reads like a description of the
// problem. Ingredients: a builder class + functions taking `Builder.() -> Unit`.

data class Route(val method: String, val path: String, val handler: () -> String)

@RouteDsl
class RoutesBuilder(private val prefix: String = "") {
    private val routes = mutableListOf<Route>()

    fun get(path: String, handler: () -> String) {
        routes += Route("GET", prefix + path, handler)
    }

    fun post(path: String, handler: () -> String) {
        routes += Route("POST", prefix + path, handler)
    }

    /** Nested group: every route inside gets the extra prefix. */
    fun group(subPrefix: String, block: RoutesBuilder.() -> Unit) {
        routes += RoutesBuilder(prefix + subPrefix).apply(block).build()
    }

    fun build(): List<Route> = routes.toList()
}

fun routes(block: RoutesBuilder.() -> Unit): List<Route> = RoutesBuilder().apply(block).build()

fun section3RouteDsl() {
    println("§3")
    val api = routes {
        get("/health") { "OK" }
        group("/api") {
            get("/books") { "[Dune, Emma]" }
            post("/books") { "created" }
        }
    }
    for (route in api) {
        println("${route.method.padEnd(4)} ${route.path.padEnd(10)} -> ${route.handler()}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. A SQL query builder (with safe parameters)
// ───────────────────────────────────────────────────────────────────────────
// Infix functions (lesson 20) make conditions read naturally: "price" lessThan 20.
//
// 🔐 Security: values are NOT glued into the SQL text. They become `?`
// placeholders plus a separate parameter list. Gluing user input into SQL is
// how "SQL injection" attacks happen. Module 03 explains this in depth.

data class Query(val sql: String, val params: List<Any?>)

@QueryDsl
class WhereBuilder {
    internal val conditions = mutableListOf<String>()
    internal val params = mutableListOf<Any?>()

    infix fun String.eq(value: Any?) = add("$this = ?", value)
    infix fun String.lessThan(value: Any) = add("$this < ?", value)
    infix fun String.like(pattern: String) = add("$this LIKE ?", pattern)

    private fun add(condition: String, value: Any?) {
        conditions += condition
        params += value
    }
}

@QueryDsl
class SelectBuilder(private val columns: List<String>) {
    private var table = ""
    private val where = WhereBuilder()
    private var orderBy: String? = null
    private var limit: Int? = null

    fun from(table: String) { this.table = table }
    fun where(block: WhereBuilder.() -> Unit) = where.block()
    fun orderBy(column: String) { orderBy = column }
    fun limit(count: Int) { limit = count }

    fun build(): Query {
        val sql = buildString {
            append("SELECT ${columns.joinToString()} FROM $table")
            if (where.conditions.isNotEmpty()) append(" WHERE ${where.conditions.joinToString(" AND ")}")
            orderBy?.let { append(" ORDER BY $it") }
            limit?.let { append(" LIMIT $it") }
        }
        return Query(sql, where.params.toList())
    }
}

fun select(vararg columns: String, block: SelectBuilder.() -> Unit): Query =
    SelectBuilder(columns.toList()).apply(block).build()

fun section4QueryDsl() {
    println("§4")
    val query = select("id", "title") {
        from("book")
        where {
            "price" lessThan 20
            "author" eq "Ada'; DROP TABLE book; --"     // harmless: it's just a parameter value
        }
        orderBy("title")
        limit(10)
    }
    println(query.sql)
    println(query.params)
}

// ───────────────────────────────────────────────────────────────────────────
// §5. @DslMarker — stop reaching into the outer builder by accident
// ───────────────────────────────────────────────────────────────────────────
// Inside `where { ... }` BOTH receivers are visible: WhereBuilder (inner) and
// SelectBuilder (outer). Without protection you could write `limit(5)` inside
// `where { }` and it would silently call the OUTER builder — confusing.
//
// Mark your builder classes with an annotation that is itself marked
// @DslMarker. Then the compiler forbids implicit calls to an OUTER receiver:
//
//     where {
//         limit(5)     // ❌ 'fun limit(count: Int): Unit' can't be called in this context by implicit receiver.
//     }
//
// (You can still be explicit: `this@select.limit(5)` — but now it's obvious.)

@DslMarker
annotation class QueryDsl

@DslMarker
annotation class RouteDsl

fun section5DslMarker() {
    println("§5")
    val query = select("title") {
        from("book")
        where {
            "title" like "D%"
            this@select.limit(5)        // explicit label: allowed, and clearly "the OUTER builder"
        }
    }
    println(query.sql)
}

// 🏗️ In the real world you've ALREADY seen these DSLs:
//
//   Gradle (build.gradle.kts):        Spring Security (module 06):
//   dependencies {                    http {
//       implementation(libs.x)            authorizeHttpRequests {
//   }                                         authorize("/api/**", authenticated)
//                                         }
//                                     }
// Each `{ }` is a lambda with a receiver (DependencyHandler, HttpSecurityDsl, …).
//
// ⚠️ Gotcha: `this` inside a receiver lambda means the RECEIVER, not your
// class. If you need the outer object, use a label: `this@MyClass`.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `T.() -> R` = lambda whose `this` is a T; call members without a prefix.
//  • DSL recipe: builder class + `fun dsl(block: Builder.() -> Unit) = Builder().apply(block).build()`.
//  • Infix + extension functions inside the builder make it read like prose.
//  • @DslMarker prevents accidental calls to outer builders.
//  • Gradle and Spring Security Kotlin configs are exactly this pattern.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// HELLO!
// HI!
// HEY!
// §2
// [1, 2, 3]
// Kotlin 2.4
// §3
// GET  /health    -> OK
// GET  /api/books -> [Dune, Emma]
// POST /api/books -> created
// §4
// SELECT id, title FROM book WHERE price < ? AND author = ? ORDER BY title LIMIT 10
// [20, Ada'; DROP TABLE book; --]
// §5
// SELECT title FROM book WHERE title LIKE ? LIMIT 5
