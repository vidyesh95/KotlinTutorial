/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 23 — Generics, part 1: boxes with labels
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : understand what `<T>` means, write generic classes, functions and
 *         interfaces, and see why `Any` is a bad substitute.
 *  Needs: lessons 15–22 (classes, interfaces, data classes)
 *  Time : ~30 min
 *  Next : lesson 24 — generic constraints (`T : Comparable<T>`)
 *  Exercises: exercises/ex23/Ex23.kt
 */
package lesson23_generics_intro

fun main() {
    section1TheProblem()
    section2GenericClass()
    section3SeveralTypeParameters()
    section4GenericFunctions()
    section5GenericInterfaces()
    section6NullableTypeArguments()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The problem generics solve
// ───────────────────────────────────────────────────────────────────────────
// Say we want a box that holds one thing. Without generics we either write one
// box per type (lots of copy-paste)…

class IntBox(var item: Int)
class StringBox(var item: String)

// …or one box that holds `Any`. `Any` is the parent of every non-null type, so
// anything fits. But when we take the item OUT, Kotlin only knows "it is Any".
// We must cast it back ourselves, and a wrong cast crashes at runtime.

class AnyBox(var item: Any)

fun section1TheProblem() {
    println("§1")
    val box = AnyBox("hello")
    val text = box.item as String          // we promise "it's a String" — compiler can't check
    println(text.uppercase())

    box.item = 42                          // nothing stops us putting an Int in later…
    val again = box.item as? String        // …so the next String-cast fails (as? gives null instead of crash)
    println("again = $again")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. A generic class
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Imagine a toy box with an empty label sticker on it. Before you use the
//    box you write on the label: "LEGO". From then on only LEGO may go in, and
//    whenever you reach inside you KNOW you'll pull out LEGO — no surprises.
//
//    `T` is that empty label. `Box<Lego>` is the box with "LEGO" written on it.
//
// Precisely: `T` is a *type parameter*. When you write `Box<String>`, `String`
// is the *type argument*. The compiler substitutes it everywhere `T` appears,
// and checks every put-in and take-out at compile time.

class Box<T>(var item: T) {
    fun replace(newItem: T): T {            // T as a parameter type and return type
        val old = item
        item = newItem
        return old
    }

    override fun toString() = "Box($item)"
}

fun section2GenericClass() {
    println("§2")
    val words: Box<String> = Box("kotlin")  // label says String
    println(words.item.uppercase())          // no cast needed: compiler knows it's a String

    val numbers = Box(10)                    // type argument inferred from 10 → Box<Int>
    val old = numbers.replace(20)
    println("old=$old, now=${numbers.item}")

    // numbers.item = "twenty"               // ❌ compile error: Type mismatch: inferred type is String but Int was expected
    println(words)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Several type parameters
// ───────────────────────────────────────────────────────────────────────────
// A class can have as many labels as it needs, separated by commas.
// Kotlin's own `Pair<A, B>` and `Map<K, V>` are built exactly like this.

data class Labelled<K, V>(val key: K, val value: V)

fun section3SeveralTypeParameters() {
    println("§3")
    val age: Labelled<String, Int> = Labelled("age", 30)
    val flag = Labelled(1, true)             // inferred: Labelled<Int, Boolean>
    println(age)
    println("${flag.key} -> ${flag.value}")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Generic functions
// ───────────────────────────────────────────────────────────────────────────
// Functions can have their own type parameters too. They go BEFORE the name:
//     fun <T> name(...)
// The caller almost never writes the type — Kotlin infers it from arguments.

fun <T> repeatItem(item: T, times: Int): List<T> = List(times) { item }

fun <T> List<T>.secondOrNull(): T? = if (size >= 2) this[1] else null   // generic extension function

fun <A, B> Pair<A, B>.flip(): Pair<B, A> = second to first

fun section4GenericFunctions() {
    println("§4")
    println(repeatItem("ha", 3))                 // T = String
    println(repeatItem<Double>(1.5, 2))          // explicit type argument, rarely needed
    println(listOf('a', 'b', 'c').secondOrNull()) // T = Char
    println(emptyList<Int>().secondOrNull())      // emptyList() alone has no clue what T is → we tell it <Int>
    println((1 to "one").flip())
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Generic interfaces — the shape you'll meet in Spring
// ───────────────────────────────────────────────────────────────────────────
// 🏗️ In the real world: Spring Data has `CrudRepository<T, ID>`. You'll write
// `interface BookRepository : CrudRepository<Book, Long>` and get save/findById
// for free. Here is a tiny version so the idea isn't magic later.

interface Repository<T, ID> {
    fun save(item: T): T
    fun findById(id: ID): T?
    fun findAll(): List<T>
}

class InMemoryRepository<T, ID>(private val idOf: (T) -> ID) : Repository<T, ID> {
    private val rows = linkedMapOf<ID, T>()

    override fun save(item: T): T {
        rows[idOf(item)] = item
        return item
    }

    override fun findById(id: ID): T? = rows[id]
    override fun findAll(): List<T> = rows.values.toList()
}

data class Book(val isbn: String, val title: String)
data class Reader(val id: Int, val name: String)

fun section5GenericInterfaces() {
    println("§5")
    // ONE implementation, reused for two completely different entity types:
    val books: Repository<Book, String> = InMemoryRepository { it.isbn }
    val readers: Repository<Reader, Int> = InMemoryRepository { it.id }

    books.save(Book("978-1", "Kotlin in Action"))
    readers.save(Reader(7, "Ada"))

    println(books.findById("978-1")?.title)
    println(readers.findById(7)?.name)
    println(readers.findById(99))
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Nullable type arguments, and naming conventions
// ───────────────────────────────────────────────────────────────────────────
// A plain `T` may be nullable: `Box<String?>` is allowed, so `item` can be null.
// (Technically `T`'s default upper bound is `Any?` — lesson 24 explains bounds.)
// If you need "T, but definitely not null" write `T & Any` (a definitely non-null type).

fun <T> orDefault(value: T, default: T & Any): T & Any = value ?: default   // result can never be null

// Naming conventions (single capital letters):
//   T = Type, E = Element (collections), K = Key, V = Value, R = Return/Result,
//   ID = identifier (common in repositories). Longer names are fine when clearer.

fun section6NullableTypeArguments() {
    println("§6")
    val maybe: Box<String?> = Box(null)
    println(maybe)
    val missing: String? = null
    println(orDefault(missing, "fallback"))
}

// ⚠️ Gotcha: generic type arguments are ERASED at runtime on the JVM. At runtime
// a `Box<String>` and a `Box<Int>` are both just `Box`. So `x is Box<String>`
// does not compile. Lesson 27 shows the fix (`reified`).

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `class Box<T>` declares a type parameter; `Box<String>` supplies a type argument.
//  • Generics = reuse code for many types WITHOUT losing type safety (unlike `Any`).
//  • Functions declare their own: `fun <T> f(x: T)`. Types are usually inferred.
//  • Same idea powers List<E>, Map<K, V>, Result<T>, and Spring's CrudRepository<T, ID>.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// HELLO
// again = null
// §2
// KOTLIN
// old=10, now=20
// Box(kotlin)
// §3
// Labelled(key=age, value=30)
// 1 -> true
// §4
// [ha, ha, ha]
// [1.5, 1.5]
// b
// null
// (one, 1)
// §5
// Kotlin in Action
// Ada
// null
// §6
// Box(null)
// fallback
