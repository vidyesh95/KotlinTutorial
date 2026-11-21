/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 15 — Classes and properties
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : declare classes, construct objects, run code at creation time,
 *         and control how properties are read and written.
 *  Needs: lessons 01–14 (variables, functions, lambdas)
 *  Time : ~35 min
 *  Next : lesson 16 — inheritance and interfaces
 *  Exercises: exercises/ex15/Ex15.kt
 */
package lesson15_classes_and_properties

fun main() {
    section1ClassAndObject()
    section2PrimaryConstructor()
    section3InitBlocksAndSecondaryConstructors()
    section4CustomGettersAndSetters()
    section5PrivateSetAndExplicitBackingFields()
    section6LateinitAndConst()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Class vs object
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A class is a COOKIE CUTTER. An object is a COOKIE. One cutter, many
//    cookies. Every cookie has the same shape, but you can put different
//    sprinkles on each one.
//
// Precisely: a class is a blueprint that describes
//   • properties — the data each object carries (the sprinkles)
//   • member functions — what each object can do
//   • constructors — how a new object is made
// An object (also called an *instance*) is one concrete thing made from it.
// Kotlin has no `new` keyword: you call the class like a function.

class Person {
    var firstName = "Alex"          // mutable property with a default value
    val lastName = "Truman"         // read-only property

    fun walk() = println("$firstName is walking.")   // member function
}

fun section1ClassAndObject() {
    println("§1")
    val alex = Person()             // cookie #1
    val jenny = Person()            // cookie #2, same cutter
    jenny.firstName = "Jenny"       // different sprinkles

    println("${alex.firstName} ${alex.lastName}")
    println("${jenny.firstName} ${jenny.lastName}")
    jenny.walk()
    println("Class name: ${alex::class.simpleName}")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Primary constructor — the class header
// ───────────────────────────────────────────────────────────────────────────
// The primary constructor lives right after the class name. Putting `val` or
// `var` in front of a parameter turns it into a property in one step.
// Parameters WITHOUT val/var are only visible during construction.
// Default values and named arguments work just like with functions.

class Car(
    val make: String,
    var color: String = "white",
    year: Int,                       // plain parameter: no property created
) {
    val age = 2026 - year            // …but usable to initialize properties

    fun describe() = "$color $make, $age years old"
}

fun section2PrimaryConstructor() {
    println("§2")
    val audi = Car("Audi", year = 2020)
    val vw = Car(make = "VW", color = "orange", year = 2025)
    vw.color = "black"
    println(audi.describe())
    println(vw.describe())
    // println(audi.year)            // ❌ Unresolved reference 'year': it is not a property
}

// ───────────────────────────────────────────────────────────────────────────
// §3. init blocks and secondary constructors
// ───────────────────────────────────────────────────────────────────────────
// `init { }` blocks run while the object is being built — in the order they
// appear, interleaved with property initializers. Use them for validation.
//
// A secondary constructor (`constructor(...)`) is an extra way to build the
// object. It MUST delegate to the primary one with `: this(...)` first, so the
// init blocks always run before the secondary constructor's own body.
//
// 🏗️ In the real world: prefer default arguments over secondary constructors.
//    Secondary constructors mostly appear when extending Java classes.

class Account(val owner: String, val balance: Long) {
    val label = "acct-$owner".also { println("  1. property initializer") }

    init {
        require(balance >= 0) { "balance must not be negative" }
        println("  2. init block (balance=$balance)")
    }

    constructor(owner: String) : this(owner, balance = 0) {
        println("  3. secondary constructor body")
    }
}

fun section3InitBlocksAndSecondaryConstructors() {
    println("§3")
    println("primary:")
    Account("ada", 100)
    println("secondary:")
    val empty = Account("bob")
    println("label=${empty.label}")

    val error = runCatching { Account("eve", -5) }.exceptionOrNull()
    println("invalid -> ${error?.message}")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Custom getters, setters and `field`
// ───────────────────────────────────────────────────────────────────────────
// Every property has a getter (and a `var` also a setter). Kotlin writes them
// for you, but you can replace them.
//
// 🧒 Like I'm 5:
//    A property is a shop counter. You never touch the shelf behind it
//    yourself — you ask the shopkeeper (getter) for things and hand things
//    to the shopkeeper (setter). The shelf behind the counter is `field`.
//    A careful shopkeeper can check what you hand over before shelving it.
//
// • `get() = …` with no `field` → computed every time, nothing is stored.
// • Inside a setter, `field` is the hidden storage ("backing field").
//   Writing `age = value` inside the setter would call the setter again
//   → infinite recursion. Always assign to `field`.

class Profile(val firstName: String, val lastName: String) {
    val fullName: String                   // computed property: no storage
        get() = "$firstName $lastName"

    var age: Int = 0
        set(value) {
            field = if (value < 0) 0 else value   // validate before storing
        }

    var nickname: String = ""
        get() = field.ifEmpty { firstName }       // fall back when empty
        set(value) {
            field = value.trim()
        }
}

fun section4CustomGettersAndSetters() {
    println("§4")
    val p = Profile("Ada", "Lovelace")
    p.age = -15
    println("${p.fullName}, age ${p.age}, nickname ${p.nickname}")
    p.nickname = "  Countess  "
    p.age = 36
    println("${p.fullName}, age ${p.age}, nickname ${p.nickname}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Read-only from outside: `private set` and 🆕 explicit backing fields
// ───────────────────────────────────────────────────────────────────────────
// Common need: "everyone may READ this, only the class may CHANGE it".
//
// Option A — `private set`: the setter is private, the getter public.
//
// Option B — the classic "backing property" (two properties, `_items` private
//   and mutable, `items` public and read-only). You'll see this in older code:
//       private val _items = mutableListOf<String>()
//       val items: List<String> get() = _items
//
// 🆕 Kotlin 2.4 (stable): explicit backing fields do option B in ONE line.
//   The property's public type is `List<String>`, but its stored `field` is a
//   `MutableList<String>`. Inside the class, Kotlin smart-casts it to mutable.
//   Outside, callers only ever see a read-only `List`.

class Counter {
    var count: Int = 0
        private set                        // public get, private set

    fun increment() {
        count++
    }
}

class ShoppingCart {
    val items: List<String>
        field = mutableListOf()            // 🆕 explicit backing field

    fun add(item: String) {
        items.add(item)                    // inside: treated as MutableList
    }
}

fun section5PrivateSetAndExplicitBackingFields() {
    println("§5")
    val counter = Counter()
    repeat(3) { counter.increment() }
    // counter.count = 100                 // ❌ Cannot assign to 'count': the setter is private
    println("count=${counter.count}")

    val cart = ShoppingCart()
    cart.add("milk")
    cart.add("bread")
    // cart.items.add("cake")              // ❌ outside, items is a read-only List
    println("cart=${cart.items}")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. lateinit and const
// ───────────────────────────────────────────────────────────────────────────
// A non-null property must be initialized when the object is created. Sometimes
// the value arrives later (a framework or test setup fills it in).
// `lateinit var` says: "I promise to set this before reading it."
//   • only for `var`, only non-null, only non-primitive types (not Int/Boolean)
//   • reading before setting throws UninitializedPropertyAccessException
//   • `this::prop.isInitialized` checks safely
//
// `const val` = a compile-time constant (String or primitive, top level or in
// an object/companion). The value is copied into every place that uses it.
//
// 🏗️ In the real world: old Spring code used `@Autowired lateinit var repo`.
//    Modern Kotlin + Spring uses constructor parameters instead (lesson DI-02).

const val MAX_RETRIES = 3

class Report {
    lateinit var title: String

    fun describe() = if (this::title.isInitialized) "Report '$title'" else "Report (untitled)"
}

fun section6LateinitAndConst() {
    println("§6")
    val report = Report()
    println(report.describe())
    val crash = runCatching { report.title.length }.exceptionOrNull()
    println("read too early -> ${crash?.let { it::class.simpleName }}")
    report.title = "Q3 sales"
    println(report.describe())
    println("MAX_RETRIES=$MAX_RETRIES")
}

// ⚠️ Gotcha: a property declared in the class body is initialized in TEXT
// ORDER. Using a property above the line where it gets its value reads the
// default (0 / null / false) — a classic source of surprise nulls.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Class = cookie cutter, object = cookie. Create with `ClassName(...)`, no `new`.
//  • `val`/`var` in the primary constructor create properties directly.
//  • `init` blocks validate; secondary constructors must call `this(...)`.
//  • Custom `get()`/`set(value)`; store via `field`, never via the property name.
//  • Public-read/private-write: `private set`, or 🆕 `field = mutableListOf()`.
//  • `lateinit var` = "filled in later"; `const val` = compile-time constant.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Alex Truman
// Jenny Truman
// Jenny is walking.
// Class name: Person
// §2
// white Audi, 6 years old
// black VW, 1 years old
// §3
// primary:
//   1. property initializer
//   2. init block (balance=100)
// secondary:
//   1. property initializer
//   2. init block (balance=0)
//   3. secondary constructor body
// label=acct-bob
//   1. property initializer
// invalid -> balance must not be negative
// §4
// Ada Lovelace, age 0, nickname Ada
// Ada Lovelace, age 36, nickname Countess
// §5
// count=3
// cart=[milk, bread]
// §6
// Report (untitled)
// read too early -> UninitializedPropertyAccessException
// Report 'Q3 sales'
// MAX_RETRIES=3
