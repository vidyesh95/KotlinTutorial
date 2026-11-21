/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 05 — Build your own DI container
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : take the magic out of Spring by building a mini container that
 *         reads constructors and wires objects automatically.
 *  Needs: lessons 01–04; module 01 lessons 18, 23–27 (generics, reified), 29 (DSLs)
 *  Time : ~45 min (read Container.kt in this folder side by side!)
 *  Next : lesson 06 — the real thing: Spring's IoC container
 *  Exercises: exercises/ex05/Ex05.kt
 */
package lesson05_build_a_container

import kotlin.reflect.full.primaryConstructor

fun main() {
    section1ReadingConstructorsAtRuntime()
    section2Autowiring()
    section3InterfacesAndFactories()
    section4Lifetimes()
    section5HelpfulErrors()
}

// The same little shop as before.
data class ShopConfig(val shopName: String, val currency: String = "EUR")

interface Notifier {
    fun send(message: String)
}

class ConsoleNotifier(private val config: ShopConfig) : Notifier {
    override fun send(message: String) = println("  [${config.shopName}] $message")
}

class BookRepository {
    private val books = mutableListOf<String>()
    fun add(title: String) { books += title }
    fun count() = books.size
}

class BookService(private val repository: BookRepository, private val notifier: Notifier) {
    fun addBook(title: String) {
        repository.add(title)
        notifier.send("added $title (${repository.count()} books)")
    }
}

class BookController(private val service: BookService) {
    fun post(title: String) = service.addBook(title)
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Reflection: reading a class's "ingredients list" at runtime
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Every LEGO set's box has a picture of the parts it needs. A robot that
//    can READ those pictures can fetch the parts and build ANY set by itself —
//    even sets invented after the robot was made.
//
//    Reflection = the program reading its own "boxes" (classes) while running:
//    "what constructor does BookService have? It needs a BookRepository and a
//    Notifier." That's all a DI container needs to build things automatically.

fun section1ReadingConstructorsAtRuntime() {
    println("§1")
    val constructor = BookService::class.primaryConstructor!!
    for (parameter in constructor.parameters) {
        println("BookService needs '${parameter.name}' of type ${parameter.type}")
    }
}
// (Needs the `kotlin-reflect` library — see this module's build.gradle.kts.
//  Spring uses it too, to understand Kotlin classes.)

// ───────────────────────────────────────────────────────────────────────────
// §2. Autowiring: let the container build the whole graph
// ───────────────────────────────────────────────────────────────────────────
// We only REGISTER types. The container works out the order, by recursion:
//   get<BookController>()
//     → needs BookService → needs BookRepository (build it) + Notifier (build it)
//     → build BookService → build BookController. Done.
// That recursion is `resolve()` in Container.kt. Go read it now. 🙂

fun section2Autowiring() {
    println("§2")
    val app = container {
        singleton { ShopConfig(shopName = "Kotlin Books") }   // config: we build it ourselves
        bind<Notifier, ConsoleNotifier>()                     // interface → implementation
        autowire<BookRepository>()
        autowire<BookService>()
        autowire<BookController>()
    }
    val controller = app.get<BookController>()   // one call, whole graph built
    controller.post("Dune")
    controller.post("Emma")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Swapping an implementation = changing ONE registration
// ───────────────────────────────────────────────────────────────────────────

class LoudNotifier : Notifier {
    override fun send(message: String) = println("  !!! ${message.uppercase()} !!!")
}

fun section3InterfacesAndFactories() {
    println("§3")
    val app = container {
        bind<Notifier, LoudNotifier>()          // the only line that changed
        autowire<BookRepository>()
        autowire<BookService>()
    }
    app.get<BookService>().addBook("Ulysses")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Lifetimes: singleton vs transient
// ───────────────────────────────────────────────────────────────────────────

class ShoppingCart {
    val items = mutableListOf<String>()
}

fun section4Lifetimes() {
    println("§4")
    val app = container {
        autowire<BookRepository>()                                  // SINGLETON by default
        autowire<ShoppingCart>(Container.Lifetime.TRANSIENT)        // fresh cart each time
    }
    println("repository shared: ${app.get<BookRepository>() === app.get<BookRepository>()}")
    println("cart shared:       ${app.get<ShoppingCart>() === app.get<ShoppingCart>()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Errors a good container explains clearly
// ───────────────────────────────────────────────────────────────────────────

class ChickenService(val egg: EggService)
class EggService(val chicken: ChickenService)

fun section5HelpfulErrors() {
    println("§5")
    val missing = container {
        autowire<BookRepository>()
        autowire<BookService>()            // forgot to bind Notifier!
    }
    println(runCatching { missing.get<BookService>() }.exceptionOrNull()?.message)

    val cyclic = container {
        autowire<ChickenService>()
        autowire<EggService>()
    }
    println(runCatching { cyclic.get<ChickenService>() }.exceptionOrNull()?.message)
}
// ⚠️ Gotcha: these errors happen when you call get(), at RUNTIME. Spring is
// smarter: it builds all singletons at STARTUP, so a missing bean crashes the
// app immediately on launch, not on the first unlucky request ("fail fast").

// What Spring adds on top of our ~100 lines:
//  • component scanning (find classes by annotation instead of registering by hand)
//  • more scopes (request, session), lifecycle callbacks (init/destroy)
//  • qualifiers & @Primary for multiple implementations of one interface
//  • proxies for @Transactional / @Cacheable / security (they wrap your beans)
//  • configuration properties, profiles, events, … and Spring Boot's auto-config
//
// 🏗️ Our container ↔ Spring:
//   autowire<T>()            ↔  @Component on class T
//   bind<I, Impl>()          ↔  @Component on Impl (Spring matches it to I by type)
//   singleton { … }          ↔  @Bean fun …() in a @Configuration class
//   Lifetime.TRANSIENT       ↔  @Scope("prototype")
//   get<T>()                 ↔  context.getBean<T>()
//   "Circular dependency…"   ↔  BeanCurrentlyInCreationException

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • A DI container = registry of bindings + recursive resolve + cache of singletons.
//  • Reflection reads constructor parameters, so wiring is automatic ("autowiring").
//  • Path tracking gives cycle detection and "needed by" messages.
//  • Spring is this idea, industrial-strength. You now know what it does inside.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// BookService needs 'repository' of type lesson05_build_a_container.BookRepository
// BookService needs 'notifier' of type lesson05_build_a_container.Notifier
// §2
//   [Kotlin Books] added Dune (1 books)
//   [Kotlin Books] added Emma (2 books)
// §3
//   !!! ADDED ULYSSES (1 BOOKS) !!!
// §4
// repository shared: true
// cart shared:       false
// §5
// No binding for Notifier (needed by BookService)
// Circular dependency: ChickenService -> EggService -> ChickenService
