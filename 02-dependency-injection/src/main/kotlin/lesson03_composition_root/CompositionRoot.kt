/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 03 — The composition root and object lifetimes
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : wire a whole app by hand in ONE place, and choose how long each
 *         object lives: singleton, new-every-time (transient), per-request (scoped).
 *  Needs: lesson 02, module 01 lesson 21 (`by lazy`)
 *  Time : ~30 min
 *  Next : lesson 04 — anti-patterns: service locator, over-injection, cycles
 *  Exercises: exercises/ex03/Ex03.kt
 */
package lesson03_composition_root

fun main() {
    section1TheObjectGraph()
    section2AHandWrittenModule()
    section3Lifetimes()
    section4OverridesForTests()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Every app is a graph of objects
// ───────────────────────────────────────────────────────────────────────────
//
//   BookController
//        │ needs
//   BookService ───────────┐
//        │ needs           │ needs
//   BookRepository      Notifier
//        │ needs           │ needs
//     Database          AppConfig (senderAddress)
//        │ needs
//     AppConfig (databaseUrl)
//
// Someone has to build this graph bottom-up: config first, then database,
// then repository, … controller last. The ONE place where that happens is
// called the *composition root*. In a plain app it's main(). In Spring it's
// the container.
//
// 🧒 Like I'm 5:
//    Building a LEGO castle from a booklet: you can't put the tower on before
//    the walls exist. The composition root is the page of the booklet where the
//    whole castle gets assembled — every other page only shows ONE piece.

data class AppConfig(val databaseUrl: String, val senderAddress: String)

class Database(config: AppConfig) {
    val url = config.databaseUrl
    init { println("  Database opened: $url") }
}

class BookRepository(private val db: Database) {
    private val books = mutableListOf<String>()
    fun add(title: String) { books += title }
    fun all(): List<String> = books.toList()
    fun source() = db.url
}

interface Notifier {
    fun send(message: String)
}

class EmailNotifier(private val config: AppConfig) : Notifier {
    override fun send(message: String) = println("  email from ${config.senderAddress}: $message")
}

class BookService(private val repository: BookRepository, private val notifier: Notifier) {
    fun addBook(title: String) {
        repository.add(title)
        notifier.send("added '$title'")
    }

    fun list() = repository.all()
}

class BookController(private val service: BookService) {
    fun post(title: String) = service.addBook(title)
    fun get() = "GET /books -> ${service.list()}"
}

fun section1TheObjectGraph() {
    println("§1")
    // Composition root, written out by hand. Read it bottom-up in the diagram above.
    val config = AppConfig(databaseUrl = "jdbc:sqlite:shop.db", senderAddress = "shop@example.com")
    val database = Database(config)
    val repository = BookRepository(database)
    val notifier = EmailNotifier(config)
    val service = BookService(repository, notifier)
    val controller = BookController(service)

    controller.post("Dune")
    println(controller.get())
}

// ───────────────────────────────────────────────────────────────────────────
// §2. A hand-written "module": the composition root as a class
// ───────────────────────────────────────────────────────────────────────────
// Bigger apps put the wiring in a class. `by lazy` gives us:
//  • each object created at most ONCE (a singleton within this module)
//  • created only when first needed, in the right order, automatically

class AppModule(val config: AppConfig) {
    val database: Database by lazy { Database(config) }
    val repository: BookRepository by lazy { BookRepository(database) }
    val notifier: Notifier by lazy { EmailNotifier(config) }
    val service: BookService by lazy { BookService(repository, notifier) }
    val controller: BookController by lazy { BookController(service) }
}

fun section2AHandWrittenModule() {
    println("§2")
    val app = AppModule(AppConfig("jdbc:sqlite:module.db", "hello@example.com"))
    println("module created, nothing built yet")
    app.controller.post("Emma")              // asking for the controller builds the whole chain
    println("same repository everywhere: ${app.repository === app.repository}")
    println("repository uses ${app.repository.source()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Lifetimes (a.k.a. scopes)
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    • The school building: there's ONE, everybody shares it.        → SINGLETON
//    • A paper cup at the water cooler: fresh one every time.        → TRANSIENT
//    • Your name tag on a field trip: one per kid PER TRIP.          → SCOPED
//
//  SINGLETON — one shared instance for the whole app. Default choice for
//              stateless services, repositories, clients. Must be thread-safe!
//  TRANSIENT — a new instance every time someone asks. For small objects with
//              their own state (a shopping cart builder, a parser).
//  SCOPED    — one instance per "unit of work", e.g. per HTTP request. Holds
//              request data: current user, request id, open transaction.

class RequestContext(val requestId: Int, val user: String)

class ShopModule {
    private var nextRequestId = 1

    val repository: BookRepository by lazy {            // singleton
        BookRepository(Database(AppConfig("jdbc:sqlite:scoped.db", "-")))
    }

    fun newCart(): MutableList<String> = mutableListOf()  // transient: a factory function

    fun <T> inRequest(user: String, block: (RequestContext) -> T): T {   // scoped
        val context = RequestContext(nextRequestId++, user)   // created when the request starts…
        return block(context)                                 // …and dropped when it ends
    }
}

fun section3Lifetimes() {
    println("§3")
    val shop = ShopModule()
    println("singleton: ${shop.repository === shop.repository}")
    println("transient: ${shop.newCart() === shop.newCart()}")
    shop.inRequest("ada") { ctx -> println("request #${ctx.requestId} by ${ctx.user}") }
    shop.inRequest("bob") { ctx -> println("request #${ctx.requestId} by ${ctx.user}") }
}
// ⚠️ Gotcha — "captive dependency": never inject a SCOPED object (e.g. the
// current user) into a SINGLETON. The singleton lives forever and would keep
// the FIRST request's user for everybody. Pass it per call instead (method
// injection, lesson 02) — or use context parameters (module 01, lesson 28).

// ───────────────────────────────────────────────────────────────────────────
// §4. Overriding pieces for tests
// ───────────────────────────────────────────────────────────────────────────
// Make the parts you may want to replace constructor parameters with
// defaults. Production uses the defaults; tests pass fakes.

class TestableModule(
    config: AppConfig,
    notifier: Notifier = EmailNotifier(config),    // default = production
) {
    val service = BookService(BookRepository(Database(config)), notifier)
}

class SilentNotifier : Notifier {
    val messages = mutableListOf<String>()
    override fun send(message: String) { messages += message }
}

fun section4OverridesForTests() {
    println("§4")
    val fake = SilentNotifier()
    val module = TestableModule(AppConfig("jdbc:sqlite::memory:", "-"), notifier = fake)
    module.service.addBook("Ulysses")
    println("captured by fake: ${fake.messages}")
}

// 🏗️ In the real world: Spring Boot IS this AppModule, generated for you.
// Singleton is Spring's default scope; "request" scope exists for web apps;
// and @TestConfiguration / @MockitoBean replace beans in tests — same idea as §4.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Composition root = the single place where the object graph is built.
//  • A hand-written module with `by lazy` = singletons, built in the right order.
//  • Lifetimes: singleton (shared), transient (new each time), scoped (per request).
//  • Don't put short-lived things inside long-lived ones (captive dependency).
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   Database opened: jdbc:sqlite:shop.db
//   email from shop@example.com: added 'Dune'
// GET /books -> [Dune]
// §2
// module created, nothing built yet
//   Database opened: jdbc:sqlite:module.db
//   email from hello@example.com: added 'Emma'
// same repository everywhere: true
// repository uses jdbc:sqlite:module.db
// §3
//   Database opened: jdbc:sqlite:scoped.db
// singleton: true
// transient: false
// request #1 by ada
// request #2 by bob
// §4
//   Database opened: jdbc:sqlite::memory:
// captured by fake: [added 'Ulysses']
