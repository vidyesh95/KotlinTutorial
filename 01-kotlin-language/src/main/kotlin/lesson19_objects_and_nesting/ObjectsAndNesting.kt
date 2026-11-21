/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 19 — Objects, companion objects, nested and inner classes
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : create singletons with `object`, one-off objects with object
 *         expressions, class-level members with `companion object`, and know
 *         when a class inside a class can see its outer object.
 *  Needs: lessons 15–18
 *  Time : ~30 min
 *  Next : lesson 20 — extensions and operators
 *  Exercises: exercises/ex19/Ex19.kt
 */
package lesson19_objects_and_nesting

fun main() {
    section1ObjectDeclarations()
    section2ObjectExpressions()
    section3CompanionObjects()
    section4FactoryFunctions()
    section5NestedVsInner()
    section6NestedTypeAliases()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Object declarations — exactly one, ever
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A school has MANY pupils but only ONE headmaster's office. You don't
//    build a new office every time you need it — everybody walks to the same
//    one. `object` is that single office.
//
// `object Name { … }` declares a class AND its only instance at once (a
// *singleton*). It's created lazily the first time it's used, thread-safely.
// No constructor parameters (there's only one, who would pass them?).

object AppConfig {
    val appName = "Bookstore"
    var requestCount = 0
        private set

    fun handleRequest() {
        requestCount++
    }
}

fun section1ObjectDeclarations() {
    println("§1")
    AppConfig.handleRequest()                 // use it by its name — no ()
    AppConfig.handleRequest()
    println("${AppConfig.appName} handled ${AppConfig.requestCount} requests")
}

// ⚠️ Gotcha: a singleton with MUTABLE state is global state. Every part of the
// program shares it, tests leak into each other, and threads race on it.
// Keep objects stateless (constants, pure helpers) where you can.
//
// 🏗️ In the real world: Spring beans are singletons too — but Spring creates
//    them and hands them to you (DI), so you can still swap them in tests.

// ───────────────────────────────────────────────────────────────────────────
// §2. Object expressions — anonymous, one-off objects
// ───────────────────────────────────────────────────────────────────────────
// Need an object that implements an interface just once, right here?
// `object : SomeInterface { … }` creates it inline without naming a class.
// It can capture local variables (it's a closure, like a lambda).

interface Greeter {
    fun greet(name: String): String
}

fun section2ObjectExpressions() {
    println("§2")
    var greetings = 0
    val polite = object : Greeter {
        override fun greet(name: String): String {
            greetings++                           // captures and changes a local variable
            return "Good morning, $name."
        }
    }
    println(polite.greet("Ada"))
    println(polite.greet("Alan"))
    println("greetings=$greetings")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Companion objects — members that belong to the CLASS
// ───────────────────────────────────────────────────────────────────────────
// Kotlin has no `static`. Things that belong to the class itself rather than
// to one instance go into a `companion object` inside the class. You call
// them through the class name: `Ticket.PREFIX`, `Ticket.issued`.
//
// 🧒 Like I'm 5: every ticket printed by a ticket machine is an instance.
//    The machine's counter ("tickets printed today: 3") belongs to the
//    MACHINE, not to any single ticket. The companion object is the machine.

class Ticket private constructor(val number: Int) {     // private: only the companion may construct
    override fun toString() = "$PREFIX-$number"

    companion object {
        const val PREFIX = "TCK"
        var issued = 0
            private set

        fun next(): Ticket = Ticket(++issued)
    }
}

fun section3CompanionObjects() {
    println("§3")
    val tickets = List(3) { Ticket.next() }
    println(tickets)
    println("issued=${Ticket.issued}, prefix=${Ticket.PREFIX}")
    // Ticket(99)                               // ❌ Cannot access '<init>': it is private
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Factory functions on the companion
// ───────────────────────────────────────────────────────────────────────────
// A factory is a function that creates objects for you — useful when creation
// needs validation, parsing, or can fail. Convention: `of`, `from…`,
// `parse`, and `…OrNull` for the version that returns null instead of throwing.
//
// 🏗️ In the real world: `UUID.fromString`, `LocalDate.parse`, `Duration.ofSeconds`.

// ⚠️ Gotcha: a data class's generated `copy()` would let anyone bypass the private
// constructor (`t.copy(celsius = -999.0)`). 🆕 @ConsistentCopyVisibility makes copy()
// exactly as private as the constructor.
@ConsistentCopyVisibility
data class Temperature private constructor(val celsius: Double) {
    companion object {
        fun ofCelsius(c: Double) = Temperature(c)
        fun ofFahrenheit(f: Double) = Temperature((f - 32) * 5 / 9)
        fun parseOrNull(text: String): Temperature? {
            val number = text.removeSuffix("C").trim().toDoubleOrNull() ?: return null
            return Temperature(number)
        }
    }
}

fun section4FactoryFunctions() {
    println("§4")
    println(Temperature.ofCelsius(21.5))
    println(Temperature.ofFahrenheit(212.0))
    println(Temperature.parseOrNull("18 C"))
    println(Temperature.parseOrNull("warm"))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Nested vs inner classes
// ───────────────────────────────────────────────────────────────────────────
// A class declared inside another class is NESTED by default: it's just
// grouped there for organisation and CANNOT see the outer object.
// Mark it `inner` and each inner object remembers the outer object that made
// it, reachable as `this@Outer`.
//
// 🧒 Like I'm 5: a nested class is a toy stored in your toy box — it lives
//    there but doesn't know who you are. An inner class is your own hand —
//    it's always attached to YOU.
//
// ⚠️ Gotcha: inner objects keep the outer object alive (memory). Default to
//    nested; use `inner` only when you really need the outer instance.

class Library(val city: String) {
    private val books = mutableListOf<String>()

    class Shelf(val label: String)                  // nested: no access to `city` or `books`

    inner class Librarian(val name: String) {       // inner: sees the Library that created it
        fun shelve(title: String) {
            books += title
            println("$name shelved '$title' in the ${this@Library.city} library (${books.size} books)")
        }
    }
}

fun section5NestedVsInner() {
    println("§5")
    val shelf = Library.Shelf("A-Z")                // created WITHOUT a Library
    println("shelf ${shelf.label}")

    val paris = Library("Paris")
    val anna = paris.Librarian("Anna")              // created FROM a Library instance
    anna.shelve("Les Misérables")
    anna.shelve("Candide")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. 🆕 Nested type aliases
// ───────────────────────────────────────────────────────────────────────────
// A type alias gives an existing type a shorter or clearer name (lesson 22).
// 🆕 Since Kotlin 2.3 (stable) you can declare one INSIDE a class, so the name
// stays scoped to where it makes sense instead of polluting the whole package.

class Graph {
    typealias Node = String
    typealias Edges = Map<Node, List<Node>>

    private val edges: Edges = mapOf("A" to listOf("B", "C"), "B" to listOf("C"), "C" to emptyList())

    fun neighbours(node: Node): List<Node> = edges[node].orEmpty()
}

fun section6NestedTypeAliases() {
    println("§6")
    val graph = Graph()
    println("A -> ${graph.neighbours("A")}")
    println("C -> ${graph.neighbours("C")}")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `object X` = class + its only instance (singleton), lazily created.
//  • `object : Interface { … }` = anonymous one-off object, can capture locals.
//  • `companion object` = class-level members (no `static` in Kotlin); great for
//    constants, counters and factory functions like `of`, `parseOrNull`.
//  • Nested class: no outer access. `inner` class: tied to an outer instance (`this@Outer`).
//  • 🆕 Type aliases can be nested inside classes.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Bookstore handled 2 requests
// §2
// Good morning, Ada.
// Good morning, Alan.
// greetings=2
// §3
// [TCK-1, TCK-2, TCK-3]
// issued=3, prefix=TCK
// §4
// Temperature(celsius=21.5)
// Temperature(celsius=100.0)
// Temperature(celsius=18.0)
// null
// §5
// shelf A-Z
// Anna shelved 'Les Misérables' in the Paris library (1 books)
// Anna shelved 'Candide' in the Paris library (2 books)
// §6
// A -> [B, C]
// C -> []
