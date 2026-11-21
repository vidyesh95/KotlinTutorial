/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 02 — Constructor injection, interfaces and fakes
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : depend on INTERFACES, inject them through the constructor, and use
 *         fakes to test without sending real emails or reading the real clock.
 *  Needs: lesson 01
 *  Time : ~30 min
 *  Next : lesson 03 — the composition root and object lifetimes
 *  Exercises: exercises/ex02/Ex02.kt
 */
package lesson02_constructor_injection

import kotlin.time.Instant

fun main() {
    section1DependOnInterfaces()
    section2SwapImplementations()
    section3FakesForTests()
    section4OtherInjectionStyles()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Depend on what it DOES, not on what it IS
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A lamp has a plug. It works in ANY wall socket — it doesn't care whether
//    the power comes from a windmill, a dam or solar panels. The socket shape
//    is the agreement. The lamp depends on the SOCKET SHAPE, not the power plant.
//
//    In code, the socket shape is an INTERFACE. OrderService should depend on
//    "something that can notify a customer", not on "the EmailSender class".
//
// This is the Dependency Inversion Principle (the "D" in SOLID): high-level
// code (ordering rules) and low-level code (email, SMS) both depend on an
// abstraction in the middle.

interface Notifier {
    fun notify(customer: String, message: String)
}

interface OrderRepository {
    fun save(order: Order)
    fun findAll(): List<Order>
}

/** "What time is it?" is a dependency too — a sneaky one. */
fun interface TimeSource {
    fun now(): Instant
}

data class Order(val customer: String, val book: String, val placedAt: Instant)

class OrderService(
    private val repository: OrderRepository,   // constructor injection:
    private val notifier: Notifier,            // everything it needs is listed here,
    private val time: TimeSource,              // and stored in private vals
) {
    fun placeOrder(customer: String, book: String): Order {
        val order = Order(customer, book, time.now())
        repository.save(order)
        notifier.notify(customer, "Order for $book received")
        return order
    }
}

// Real ("production") implementations:
class EmailNotifier : Notifier {
    override fun notify(customer: String, message: String) = println("  email -> $customer: $message")
}

class SmsNotifier : Notifier {
    override fun notify(customer: String, message: String) = println("  sms   -> $customer: $message")
}

class InMemoryOrderRepository : OrderRepository {
    private val orders = mutableListOf<Order>()
    override fun save(order: Order) { orders += order }
    override fun findAll(): List<Order> = orders.toList()
}

val fixedTime = TimeSource { Instant.parse("2026-01-15T09:30:00Z") }

fun section1DependOnInterfaces() {
    println("§1")
    val service = OrderService(InMemoryOrderRepository(), EmailNotifier(), fixedTime)
    val order = service.placeOrder("ada", "Dune")
    println("placed at ${order.placedAt}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Swapping is now a one-line change OUTSIDE the class
// ───────────────────────────────────────────────────────────────────────────

fun section2SwapImplementations() {
    println("§2")
    val service = OrderService(InMemoryOrderRepository(), SmsNotifier(), fixedTime)   // email → SMS
    service.placeOrder("bob", "Emma")
    // OrderService.kt was not touched. That's the whole point.
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Fakes: test doubles you write yourself
// ───────────────────────────────────────────────────────────────────────────
// A *fake* is a simple, working implementation meant for tests. This one
// doesn't send anything — it just remembers what it was asked to send, so a
// test can check it afterwards.
//
// 🧒 Like I'm 5: a toy cash register. Kids can "pay" and you can look at what
// was paid, but no real money moves.

class RecordingNotifier : Notifier {
    val sent = mutableListOf<String>()
    override fun notify(customer: String, message: String) {
        sent += "$customer: $message"
    }
}

fun section3FakesForTests() {
    println("§3")
    // Arrange: build the service with fakes we control
    val repository = InMemoryOrderRepository()
    val notifier = RecordingNotifier()
    val clock = TimeSource { Instant.parse("2030-12-24T18:00:00Z") }   // time travel for free!
    val service = OrderService(repository, notifier, clock)

    // Act
    service.placeOrder("ada", "Dune")

    // Assert (a real test would use assertEquals — see module 01, lesson 35)
    println("saved: ${repository.findAll().single().book}")
    println("notified: ${notifier.sent}")
    println("timestamp: ${repository.findAll().single().placedAt}")
}
// Without DI, the clock would be `Clock.System.now()` inside the class, and a
// test for "orders placed on Christmas Eve get gift wrap" would only pass one
// day a year. Inject time!

// ───────────────────────────────────────────────────────────────────────────
// §4. Other injection styles (and why constructor injection wins)
// ───────────────────────────────────────────────────────────────────────────

// (a) Setter / property injection: object is created FIRST, filled in LATER.
class ReportPrinter {
    lateinit var notifier: Notifier            // forget to set it → crash at runtime
    fun print() = notifier.notify("boss", "report ready")
}

// (b) Method (parameter) injection: dependency passed per call — good when it
//     changes every call (e.g. the current user).
class Greeter {
    fun greet(name: String, notifier: Notifier) = notifier.notify(name, "hello!")
}

// (c) Function injection: a plain lambda as the dependency. Lightweight and
//     very Kotlin-y when the "interface" would have just one method.
class Welcomer(private val send: (String) -> Unit) {
    fun welcome(name: String) = send("Welcome, $name")
}

fun section4OtherInjectionStyles() {
    println("§4")
    val printer = ReportPrinter()
    printer.notifier = EmailNotifier()
    printer.print()

    Greeter().greet("ada", SmsNotifier())

    val outbox = mutableListOf<String>()
    Welcomer(send = { outbox += it }).welcome("bob")
    println("outbox: $outbox")
}

// Why prefer CONSTRUCTOR injection:
//  ✅ object is complete the moment it exists (no lateinit surprises)
//  ✅ dependencies can be `private val` — immutable, thread-safe
//  ✅ the constructor documents exactly what the class needs
//  ✅ too many parameters? A loud hint the class does too much (lesson 04)
//
// 🏗️ In the real world: Spring recommends constructor injection. In Kotlin a
// class with ONE constructor needs no @Autowired at all — Spring just calls it.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Depend on interfaces ("socket shapes"), not concrete classes.
//  • Inject through the constructor as `private val`s.
//  • Fakes = simple real implementations for tests (RecordingNotifier, fixed clock).
//  • Time, randomness, IDs, config are dependencies too — inject them.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   email -> ada: Order for Dune received
// placed at 2026-01-15T09:30:00Z
// §2
//   sms   -> bob: Order for Emma received
// §3
// saved: Dune
// notified: [ada: Order for Dune received]
// timestamp: 2030-12-24T18:00:00Z
// §4
//   email -> boss: report ready
//   sms   -> ada: hello!
// outbox: [Welcome, bob]
