/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 01 — What is a dependency? (and why "new-ing" it yourself hurts)
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : recognise dependencies, see the pain of hard-wiring them, and meet
 *         the idea of Dependency Injection (DI) / Inversion of Control (IoC).
 *  Needs: module 01 (classes, interfaces)
 *  Time : ~20 min
 *  Next : lesson 02 — constructor injection, interfaces and fakes
 *  Exercises: exercises/ex01/Ex01.kt
 */
package lesson01_what_is_a_dependency

fun main() {
    section1HardWired()
    section2WhatGoesWrong()
    section3TheLunchbox()
    section4InjectedVersion()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. A class that builds its own helpers
// ───────────────────────────────────────────────────────────────────────────
// A *dependency* is anything a class needs to do its job: a database
// repository, an email sender, a clock, a config value…
//
// The "obvious" way is to create them right inside the class:

class EmailSender {
    fun send(to: String, text: String) = println("  [real email] to $to: $text")
}

class OrderRepository {
    private val orders = mutableListOf<String>()
    fun save(book: String) { orders += book }
    fun count() = orders.size
}

class HardWiredOrderService {
    private val repository = OrderRepository()   // ← creates its own dependency
    private val email = EmailSender()            // ← and another one

    fun placeOrder(customer: String, book: String) {
        repository.save(book)
        email.send(customer, "Thanks for ordering $book")
    }

    fun orderCount() = repository.count()
}

fun section1HardWired() {
    println("§1")
    val service = HardWiredOrderService()
    service.placeOrder("ada@example.com", "Dune")
    println("orders: ${service.orderCount()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. What goes wrong
// ───────────────────────────────────────────────────────────────────────────
// It works… until real life shows up:
//
//  1. TESTING: every test of HardWiredOrderService sends a REAL email. You
//     can't swap EmailSender for a fake one — it's locked inside.
//  2. CHANGE: the boss wants SMS instead of email. You must edit
//     OrderService, even though ordering logic didn't change.
//  3. SHARING: two services that should share ONE repository each secretly
//     create their own. Data ends up split (see below).
//  4. HONESTY: the constructor `HardWiredOrderService()` says "I need
//     nothing". That's a lie — it needs a repository and an email sender.

fun section2WhatGoesWrong() {
    println("§2")
    val web = HardWiredOrderService()      // orders placed on the website
    val app = HardWiredOrderService()      // orders placed in the mobile app
    web.placeOrder("ada@example.com", "Dune")
    app.placeOrder("bob@example.com", "Emma")
    // We want ONE shop with 2 orders. We got two shops with 1 order each:
    println("web sees ${web.orderCount()}, app sees ${app.orderCount()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. The idea: someone else packs the lunchbox
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    At school you don't cook your own lunch. In the morning a grown-up packs
//    your lunchbox and hands it to you. You just open it and eat.
//    • Allergic to peanuts? The grown-up packs something else — YOU don't change.
//    • Field trip? They pack a sandwich instead of soup — you still just eat.
//    • Your twin gets the same kind of lunch from the same kitchen.
//
//    The kid = your class. The lunch = its dependencies. The grown-up = whoever
//    builds the objects (main(), or later a "DI container" like Spring).
//
// Precisely:
//  • Dependency Injection (DI): a class RECEIVES its dependencies from outside
//    (usually through its constructor) instead of creating them itself.
//  • Inversion of Control (IoC): the class gives up control over creating and
//    choosing its helpers. Control is "inverted" — moved outside, to the caller.

fun section3TheLunchbox() {
    println("§3")
    println("class says WHAT it needs; someone else decides HOW it's built")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Same service, with dependencies injected
// ───────────────────────────────────────────────────────────────────────────
// The only change: dependencies move from "created inside" to "constructor
// parameters". The constructor is now honest about what it needs.

class OrderService(
    private val repository: OrderRepository,
    private val email: EmailSender,
) {
    fun placeOrder(customer: String, book: String) {
        repository.save(book)
        email.send(customer, "Thanks for ordering $book")
    }
}

fun section4InjectedVersion() {
    println("§4")
    // "The grown-up packing the lunchboxes" — here it's simply main():
    val sharedRepository = OrderRepository()
    val email = EmailSender()
    val web = OrderService(sharedRepository, email)
    val app = OrderService(sharedRepository, email)   // same repository handed to both

    web.placeOrder("ada@example.com", "Dune")
    app.placeOrder("bob@example.com", "Emma")
    println("one shared repository holds ${sharedRepository.count()} orders")
}
// Problems 3 (sharing) and 4 (honesty) are solved already. Lesson 02 solves
// 1 (testing) and 2 (change) by depending on INTERFACES instead of classes.

// 🏗️ In the real world: every Spring class you'll write looks like OrderService
// above — `class BookController(private val service: BookService)` — and Spring
// plays the grown-up who builds and hands over the dependencies.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Dependency = something a class needs to work.
//  • Creating it inside (`= EmailSender()`) hard-wires it: untestable, rigid,
//    can't be shared, hidden from the constructor.
//  • DI = receive dependencies from outside, usually via the constructor.
//  • IoC = creation & choice of helpers moves out of the class.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   [real email] to ada@example.com: Thanks for ordering Dune
// orders: 1
// §2
//   [real email] to ada@example.com: Thanks for ordering Dune
//   [real email] to bob@example.com: Thanks for ordering Emma
// web sees 1, app sees 1
// §3
// class says WHAT it needs; someone else decides HOW it's built
// §4
//   [real email] to ada@example.com: Thanks for ordering Dune
//   [real email] to bob@example.com: Thanks for ordering Emma
// one shared repository holds 2 orders
