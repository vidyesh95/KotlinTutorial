/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  DI Lesson 04 — Anti-patterns: service locator, over-injection, cycles
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : recognise the classic DI mistakes so you can spot them in real code
 *         (and in Spring error messages).
 *  Needs: lessons 01–03, module 01 lesson 27 (reified)
 *  Time : ~25 min
 *  Next : lesson 05 — build your own DI container
 *  Exercises: exercises/ex04/Ex04.kt
 */
package lesson04_anti_patterns

import kotlin.reflect.KClass

fun main() {
    section1ServiceLocator()
    section2WhyItHurts()
    section3OverInjection()
    section4CircularDependencies()
}

interface Notifier {
    fun send(to: String, message: String)
}

class ConsoleNotifier : Notifier {
    override fun send(to: String, message: String) = println("  -> $to: $message")
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The service locator
// ───────────────────────────────────────────────────────────────────────────
// A global registry. Classes don't GET their dependencies — they go and FETCH
// them from the registry whenever they like.
//
// 🧒 Like I'm 5:
//    Instead of being handed a packed lunchbox, every kid walks to one giant
//    school fridge and grabs whatever they want, whenever they want.
//    Nobody can tell what a kid will eat by looking at them. And if the fridge
//    is missing the yogurt, the kid only finds out at lunchtime — hungry.

object ServiceLocator {
    private val services = mutableMapOf<KClass<*>, Any>()

    fun <T : Any> register(type: KClass<T>, instance: T) {
        services[type] = instance
    }

    fun clear() = services.clear()

    // Public so the inline function below can use it (lesson 27: reified needs inline).
    fun lookup(type: KClass<*>): Any =
        services[type] ?: error("Nothing registered for ${type.simpleName}")

    inline fun <reified T : Any> get(): T = lookup(T::class) as T
}

class LocatorOrderService {                       // constructor says "I need nothing"… 🙈
    fun placeOrder(customer: String, book: String) {
        val notifier = ServiceLocator.get<Notifier>()   // …but secretly needs a Notifier
        notifier.send(customer, "ordered $book")
    }
}

fun section1ServiceLocator() {
    println("§1")
    ServiceLocator.register(Notifier::class, ConsoleNotifier())
    LocatorOrderService().placeOrder("ada", "Dune")
    ServiceLocator.clear()
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Why it hurts
// ───────────────────────────────────────────────────────────────────────────
//  1. HIDDEN dependencies — you must read every line of the class to learn what
//     it needs. The constructor lies (same problem as lesson 01).
//  2. COMPILE TIME → RUN TIME — forgetting to register something compiles fine
//     and explodes later, maybe in production, maybe on a rarely used path.
//  3. GLOBAL STATE — tests share the registry; one test's leftovers break another.
//
// With constructor injection, forgetting a dependency is a COMPILE error:
//     OrderService(repository)   // ❌ No value passed for parameter 'notifier'

fun section2WhyItHurts() {
    println("§2")
    // Nobody registered a Notifier this time. It compiles. It runs. Then:
    val failure = runCatching { LocatorOrderService().placeOrder("bob", "Emma") }
    println("runtime failure: ${failure.exceptionOrNull()?.message}")
}
// Fair use: frameworks use a locator INTERNALLY at the composition root
// (Spring's ApplicationContext.getBean is one). Your business classes shouldn't.

// ───────────────────────────────────────────────────────────────────────────
// §3. Constructor over-injection
// ───────────────────────────────────────────────────────────────────────────
// DI makes dependencies VISIBLE — and sometimes what you see is ugly:
//
//   class CheckoutService(
//       repo, payments, tax, shipping, email, sms, audit, discounts,
//       inventory, loyalty, fraud, analytics,          // 12 dependencies!
//   )
//
// That's not a DI problem, it's a design smell: the class has too many jobs.
// DI just made it obvious. Fix: group related work into smaller services.

class PricingService { fun total(items: List<Int>) = items.sum() }
class FulfilmentService(private val notifier: Notifier) {
    fun ship(customer: String) = notifier.send(customer, "your parcel is on its way")
}

class CheckoutService(                // 2 focused collaborators instead of 12 tiny ones
    private val pricing: PricingService,
    private val fulfilment: FulfilmentService,
) {
    fun checkout(customer: String, items: List<Int>): Int {
        val total = pricing.total(items)
        fulfilment.ship(customer)
        return total
    }
}

fun section3OverInjection() {
    println("§3")
    val checkout = CheckoutService(PricingService(), FulfilmentService(ConsoleNotifier()))
    println("total = ${checkout.checkout("ada", listOf(10, 25))}")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Circular dependencies
// ───────────────────────────────────────────────────────────────────────────
// A needs B, B needs A. With constructors you literally can't build them:
//
//   class UserService(val orders: OrderHistoryService)
//   class OrderHistoryService(val users: UserService)
//   UserService(OrderHistoryService(UserService(OrderHistoryService(…   // forever
//
// 🧒 Like I'm 5: two kids each refusing to go down the slide until the other
// one goes first. Nobody ever slides.
//
// Spring refuses at startup with "The dependencies of some of the beans in the
// application context form a cycle". Fixes, best first:
//  1. Extract the shared part into a THIRD class both depend on.
//  2. Turn one direction into an event/callback.
//  3. (Last resort) lazy injection — hides the design problem.

class CustomerDirectory {                         // fix #1: the extracted third class
    private val names = mapOf(1 to "ada", 2 to "bob")
    fun name(id: Int) = names.getValue(id)
}

class UserService(private val directory: CustomerDirectory) {
    fun greet(id: Int) = "hello ${directory.name(id)}"
}

class OrderHistoryService(private val directory: CustomerDirectory) {
    fun summary(id: Int) = "${directory.name(id)} has 3 orders"
}

fun section4CircularDependencies() {
    println("§4")
    val directory = CustomerDirectory()
    println(UserService(directory).greet(1))
    println(OrderHistoryService(directory).summary(2))
}
// Lesson 05's container detects cycles and prints the chain, e.g.
// "Circular dependency: A -> B -> A". Exercise 04 makes you write that check.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Service locator = fetch-from-global-registry. Hidden deps, runtime errors.
//  • Too many constructor params = class does too much. Split it.
//  • A ↔ B cycles: extract a third class, or use events. Don't paper over them.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
//   -> ada: ordered Dune
// §2
// runtime failure: Nothing registered for Notifier
// §3
//   -> ada: your parcel is on its way
// total = 35
// §4
// hello ada
// bob has 3 orders
