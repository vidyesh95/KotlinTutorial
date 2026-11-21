/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 17 — Data classes, enum classes, value classes
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : model data with almost no code: `data class` for records,
 *         `enum class` for a fixed set of choices, `value class` for
 *         type-safe wrappers like UserId, plus `Uuid` for real IDs.
 *  Needs: lessons 15–16
 *  Time : ~35 min
 *  Next : lesson 18 — sealed hierarchies
 *  Exercises: exercises/ex17/Ex17.kt
 */
package lesson17_data_enum_value_classes

import kotlin.uuid.Uuid

fun main() {
    section1DataClasses()
    section2CopyAndDestructuring()
    section3EnumClasses()
    section4EnumsWithBehaviour()
    section5ValueClasses()
    section6Uuid()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Data classes — classes that are "just data"
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Two library cards that say "Ada, card #7" are the SAME card info, even if
//    printed on two separate pieces of plastic. A data class compares by what's
//    WRITTEN on it, not by which piece of plastic it is.
//
// `data class` makes the compiler generate, from the primary-constructor properties:
//   • equals()/hashCode()  → compare by content
//   • toString()           → "User(name=Ada, age=36)"
//   • copy()               → clone with some fields changed
//   • component1(), component2()… → destructuring
// Rules: primary constructor needs ≥ 1 parameter, all marked val/var; the class
// can't be abstract, open, sealed or inner.

class PlainUser(val name: String, val age: Int)
data class User(val name: String, val age: Int)

fun section1DataClasses() {
    println("§1")
    println(PlainUser("Ada", 36) == PlainUser("Ada", 36))   // false: two different objects
    println(User("Ada", 36) == User("Ada", 36))             // true: same content
    println(User("Ada", 36))                                // readable toString
    println(setOf(User("Ada", 36), User("Ada", 36)).size)   // hashCode agrees → set keeps 1
}

// ───────────────────────────────────────────────────────────────────────────
// §2. copy() and destructuring
// ───────────────────────────────────────────────────────────────────────────
// Prefer `val` properties in data classes. To "change" something, make a new
// object with copy(). Immutable objects are safe to share between threads and
// can't change behind your back — great for backend code.
//
// ⚠️ Gotcha: only properties in the PRIMARY constructor take part in
//    equals/hashCode/toString/copy. Properties declared in the body are ignored.

data class Order(val id: Int, val item: String, val quantity: Int = 1) {
    var note: String = ""          // body property: NOT part of equals/toString
}

fun section2CopyAndDestructuring() {
    println("§2")
    val first = Order(1, "coffee")
    val bigger = first.copy(quantity = 3)       // everything else copied as-is
    println(first)
    println(bigger)

    val (id, item, quantity) = bigger           // destructuring via component1..3
    println("id=$id item=$item quantity=$quantity")

    val a = Order(2, "tea").apply { note = "no sugar" }
    val b = Order(2, "tea").apply { note = "extra hot" }
    println("a == b ? ${a == b}")               // true! `note` is ignored
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Enum classes — a fixed list of choices
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5: a traffic light can be RED, YELLOW or GREEN. Never "purple".
//    An enum lists ALL the allowed values, so nobody can invent a new one.
//
// Each entry is a single object. You get for free:
//   • `name` and `ordinal` (position, starting at 0)
//   • `EnumClass.entries` — list of all values (prefer it over old `values()`)
//   • `EnumClass.valueOf("RED")` — look up by name (throws if unknown)
//   • exhaustive `when`: cover all entries and no `else` is needed

enum class Light { RED, YELLOW, GREEN }

fun next(light: Light): Light = when (light) {   // no `else`: compiler knows all 3 cases
    Light.RED -> Light.GREEN
    Light.GREEN -> Light.YELLOW
    Light.YELLOW -> Light.RED
}

fun section3EnumClasses() {
    println("§3")
    println(Light.entries)
    println("${Light.YELLOW.name} is at position ${Light.YELLOW.ordinal}")
    println("after RED comes ${next(Light.RED)}")
    println(Light.valueOf("GREEN") == Light.GREEN)
    println(Light.entries.firstOrNull { it.name == "PURPLE" })   // safe lookup → null
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Enums with properties and behaviour
// ───────────────────────────────────────────────────────────────────────────
// Enums are real classes: they can have a constructor, properties, functions,
// and even implement interfaces.
//
// 🏗️ In the real world: order status, user role (USER/ADMIN), currency.
//    Stored in SQL as TEXT (`status = 'SHIPPED'`) and mapped back with valueOf.

enum class OrderStatus(val label: String, val isFinal: Boolean) {
    PLACED("Placed", isFinal = false),
    SHIPPED("On its way", isFinal = false),
    DELIVERED("Delivered", isFinal = true),
    CANCELLED("Cancelled", isFinal = true);       // `;` needed before members

    fun canCancel() = this == PLACED
}

fun section4EnumsWithBehaviour() {
    println("§4")
    for (status in OrderStatus.entries) {
        println("${status.name.padEnd(9)} label='${status.label}' final=${status.isFinal} canCancel=${status.canCancel()}")
    }
    println("open statuses: ${OrderStatus.entries.filterNot { it.isFinal }}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Value classes — type-safe wrappers that cost nothing
// ───────────────────────────────────────────────────────────────────────────
// Problem: `fun transfer(from: Long, to: Long, amount: Long)` — three Longs.
// Swap two arguments and the compiler can't help you.
//
// 🧒 Like I'm 5: putting a colored sleeve on a pencil. Blue sleeve = "user
//    pencil", red sleeve = "order pencil". Same pencil inside, but you can't
//    mix them up any more. And the sleeve weighs nothing.
//
// `@JvmInline value class UserId(val value: Long)`
//   • exactly one property in the primary constructor
//   • at runtime it's usually just the raw Long (no extra object) → no cost
//   • at compile time UserId and OrderId are different types → mix-ups don't compile
//   • can have `init` validation and functions

@JvmInline
value class UserId(val value: Long)

@JvmInline
value class OrderId(val value: Long)

@JvmInline
value class Email(val value: String) {
    init {
        require("@" in value) { "not an email: $value" }
    }

    val domain: String get() = value.substringAfter("@")
}

fun cancelOrder(user: UserId, order: OrderId) = "user ${user.value} cancels order ${order.value}"

fun section5ValueClasses() {
    println("§5")
    val user = UserId(42)
    val order = OrderId(1001)
    println(cancelOrder(user, order))
    // cancelOrder(order, user)                  // ❌ Argument type mismatch: actual type is 'OrderId', but 'UserId' was expected
    println(Email("ada@example.com").domain)
    println(runCatching { Email("nope") }.exceptionOrNull()?.message)
    println(user)                                // toString shows the wrapper
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Uuid — globally unique IDs from the standard library
// ───────────────────────────────────────────────────────────────────────────
// A UUID is a 128-bit random-looking ID like 550e8400-e29b-41d4-a716-446655440000.
// Anyone can generate one without asking a database for "the next number",
// and collisions are practically impossible.
//
// 🆕 Kotlin 2.4: `kotlin.uuid.Uuid` is stable — `Uuid.random()`, `Uuid.parse()`,
//    `Uuid.parseOrNull()`. (The newer `generateV4()`/`generateV7()` functions
//    are still experimental, so we don't use them.)
//
// 🏗️ In the real world: the better-auth-style schema in module 06 uses text
//    UUIDs as primary keys. Wrap them in a value class: `value class SessionId(val value: Uuid)`.

@JvmInline
value class SessionId(val value: Uuid)

fun section6Uuid() {
    println("§6")
    val fixed = Uuid.parse("550e8400-e29b-41d4-a716-446655440000")
    println(fixed)
    println(Uuid.parseOrNull("not-a-uuid"))
    val a = Uuid.random()
    val b = Uuid.random()
    println("two random uuids equal? ${a == b}")        // random → output differs, so we only print a boolean
    println("text length = ${a.toString().length}")
    println(SessionId(fixed).value == fixed)
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `data class`: equals/hashCode/toString/copy/componentN from primary-constructor vals.
//  • Keep data classes immutable (`val`); change with `copy(...)`.
//  • `enum class`: closed set of named constants; `entries`, `name`, `ordinal`, exhaustive `when`.
//  • Enums can carry properties and functions.
//  • `@JvmInline value class`: zero-cost type-safe wrapper → no more mixed-up IDs.
//  • 🆕 `kotlin.uuid.Uuid` (stable in 2.4): random(), parse(), parseOrNull().
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// false
// true
// User(name=Ada, age=36)
// 1
// §2
// Order(id=1, item=coffee, quantity=1)
// Order(id=1, item=coffee, quantity=3)
// id=1 item=coffee quantity=3
// a == b ? true
// §3
// [RED, YELLOW, GREEN]
// YELLOW is at position 1
// after RED comes GREEN
// true
// null
// §4
// PLACED    label='Placed' final=false canCancel=true
// SHIPPED   label='On its way' final=false canCancel=false
// DELIVERED label='Delivered' final=true canCancel=false
// CANCELLED label='Cancelled' final=true canCancel=false
// open statuses: [PLACED, SHIPPED]
// §5
// user 42 cancels order 1001
// example.com
// not an email: nope
// UserId(value=42)
// §6
// 550e8400-e29b-41d4-a716-446655440000
// null
// two random uuids equal? false
// text length = 36
// true
