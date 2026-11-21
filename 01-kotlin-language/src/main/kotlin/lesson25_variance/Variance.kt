/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 25 — Generics, part 3: variance (`out` and `in`)
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : understand WHEN a `Box<Apple>` may be used as a `Box<Fruit>`, and
 *         what `out T` / `in T` in declarations like `List<out E>` mean.
 *  Needs: lessons 16 (inheritance), 23–24
 *  Time : ~40 min — the hardest generics lesson. Read it twice. Really.
 *  Next : lesson 26 — use-site variance and star projections
 *  Exercises: exercises/ex25/Ex25.kt
 */
package lesson25_variance

fun main() {
    section1TheSurprise()
    section2Vocabulary()
    section3OutProducers()
    section4InConsumers()
    section5FunctionTypes()
    section6InvariantByDefault()
}

// A tiny class hierarchy used through the whole lesson:
//
//            Fruit
//           /     \
//        Apple   Banana
//
open class Fruit(val name: String) {
    override fun toString() = name
}

class Apple(variety: String) : Fruit(variety)
class Banana : Fruit("banana")

// ───────────────────────────────────────────────────────────────────────────
// §1. The surprise
// ───────────────────────────────────────────────────────────────────────────
// An Apple IS a Fruit. So is a list of apples a list of fruit? Sometimes!

fun printAll(fruits: List<Fruit>) = println(fruits.joinToString())

fun addBanana(fruits: MutableList<Fruit>) {
    fruits.add(Banana())
}

fun section1TheSurprise() {
    println("§1")
    val apples: List<Apple> = listOf(Apple("gala"), Apple("fuji"))
    printAll(apples)                     // ✅ a List<Apple> is accepted as List<Fruit>

    val appleBasket: MutableList<Apple> = mutableListOf(Apple("gala"))
    // addBanana(appleBasket)            // ❌ MutableList<Apple> is NOT a MutableList<Fruit>
    println("basket still holds only apples: $appleBasket")
    //
    // Why refuse? If it compiled, addBanana would put a Banana into appleBasket,
    // and later `val a: Apple = appleBasket[1]` would crash: a banana is no apple.
}

// 🧒 Like I'm 5:
//    You have a basket with ONLY apples in it.
//
//    Your friend says: "Lend me a basket of fruit, I want to TAKE fruit out."
//    → Sure! Whatever they take out is an apple, and an apple is a fruit. Safe.
//
//    Another friend says: "Lend me a basket of fruit, I want to PUT fruit IN."
//    → No way! They might drop a banana into your apples-only basket.
//
//    • Basket you only TAKE from   → can pretend to be the more general basket.
//    • Basket you also PUT into    → must stay exactly what it is.
//
//    Kotlin's `List` is read-only (take-only) → List<Apple> works as List<Fruit>.
//    `MutableList` can be written to           → MutableList<Apple> does not.

// ───────────────────────────────────────────────────────────────────────────
// §2. Vocabulary (worth memorising)
// ───────────────────────────────────────────────────────────────────────────
//   Apple is a SUBTYPE of Fruit: an Apple can go wherever a Fruit is expected.
//
//   For a generic type G<T>, given Apple is a subtype of Fruit:
//   ┌───────────────┬──────────────────────────────────────┬─────────────────┐
//   │ name          │ relationship                         │ Kotlin marker   │
//   ├───────────────┼──────────────────────────────────────┼─────────────────┤
//   │ covariant     │ G<Apple> is a subtype of G<Fruit>    │ G<out T>        │
//   │ contravariant │ G<Fruit> is a subtype of G<Apple>    │ G<in T>         │
//   │ invariant     │ no relationship at all               │ G<T> (default)  │
//   └───────────────┴──────────────────────────────────────┴─────────────────┘
//   "co" = goes the SAME direction as Apple→Fruit. "contra" = the OPPOSITE one.

fun section2Vocabulary() {
    println("§2")
    val fruit: Fruit = Apple("gala")                 // plain subtyping
    val list: List<Fruit> = listOf(Apple("fuji"))   // covariance: List is declared `List<out E>`
    println("${fruit.name} is a Fruit")
    println(list)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `out T` — producers (things that GIVE you T)
// ───────────────────────────────────────────────────────────────────────────
// Mark a type parameter `out` and you promise: "T only comes OUT of me —
// as return values — never goes IN as a parameter." In return, Kotlin makes
// the type covariant: Producer<Apple> can be used as Producer<Fruit>.

interface Producer<out T> {
    fun produce(): T
    // fun eat(item: T)   // ❌ Type parameter T is declared as 'out' but occurs in 'in' position
}

class AppleTree : Producer<Apple> {
    private var picked = 0
    override fun produce() = Apple("apple#${++picked}")
}

fun harvest(source: Producer<Fruit>, count: Int): List<Fruit> = List(count) { source.produce() }

fun section3OutProducers() {
    println("§3")
    println(harvest(AppleTree(), 2))    // ✅ Producer<Apple> accepted as Producer<Fruit>
}
// 🏗️ In the real world: List<out E>, Set<out E>, Map<K, out V>, Sequence<out T>,
// Flow<out T>, Result<out T> — all read-only producers, all covariant.

// ───────────────────────────────────────────────────────────────────────────
// §4. `in T` — consumers (things that TAKE T from you)
// ───────────────────────────────────────────────────────────────────────────
// Mark it `in` and you promise: "T only goes IN (as parameters), never comes
// out." Kotlin then flips the direction: Consumer<Fruit> may be used as
// Consumer<Apple>.
//
// 🧒 Like I'm 5:
//    A juicer that can juice ANY fruit can certainly juice your apples.
//    A juicer built ONLY for apples can't be trusted with "any fruit".
//    The more things a consumer accepts, the more places it can be used.

interface Consumer<in T> {
    fun consume(item: T)
}

class FruitJuicer : Consumer<Fruit> {
    val juiced = mutableListOf<String>()
    override fun consume(item: Fruit) {
        juiced += item.name
    }
}

fun feedApples(consumer: Consumer<Apple>) {
    consumer.consume(Apple("gala"))
    consumer.consume(Apple("fuji"))
}

fun section4InConsumers() {
    println("§4")
    val juicer = FruitJuicer()
    feedApples(juicer)                  // ✅ Consumer<Fruit> accepted as Consumer<Apple>
    println(juicer.juiced)

    // The stdlib's Comparator is declared `Comparator<in T>`:
    // a comparator that can order ANY fruit can order a list of apples.
    val byName: Comparator<Fruit> = compareBy { it.name }
    val apples = listOf(Apple("gala"), Apple("fuji"), Apple("braeburn"))
    println(apples.sortedWith(byName))  // sortedWith wants Comparator<in Apple> → Comparator<Fruit> fits
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Function types use both: `(in P) -> out R`
// ───────────────────────────────────────────────────────────────────────────
// A function CONSUMES its parameters and PRODUCES its result. So:
//   (Fruit) -> Apple   can be used where   (Apple) -> Fruit   is expected.
// It accepts more (any fruit, not just apples) and returns something more
// specific (an Apple is a Fruit). Both directions are safe.

fun section5FunctionTypes() {
    println("§5")
    val ripen: (Fruit) -> Apple = { Apple("ripe ${it.name}") }
    val needed: (Apple) -> Fruit = ripen     // ✅
    println(needed(Apple("gala")))
}

// Memory trick:  out = Output = Producer = "I give you T"   (return type)
//                in  = Input  = Consumer = "you give me T"  (parameter)
// (Java folks say PECS: "Producer Extends, Consumer Super". Same idea.)

// ───────────────────────────────────────────────────────────────────────────
// §6. Invariant by default — and why that's correct
// ───────────────────────────────────────────────────────────────────────────
// If a class both takes T in AND gives T out, neither promise holds, so it
// must stay invariant: Basket<Apple> and Basket<Fruit> are unrelated types.
// MutableList<E>, Array<T> and most "storage" classes are like this.

class Basket<T> {
    private val items = mutableListOf<T>()
    fun put(item: T) { items += item }        // T goes in
    fun take(): T = items.removeAt(0)         // T comes out
}

fun section6InvariantByDefault() {
    println("§6")
    val appleBasket = Basket<Apple>()
    appleBasket.put(Apple("gala"))
    // val fruitBasket: Basket<Fruit> = appleBasket   // ❌ Type mismatch — this is the §1 banana problem
    println(appleBasket.take())
}

// ⚠️ Gotcha: variance is checked only for PUBLIC members. A `private` property
// of type T is fine inside a `class Box<out T>` because nobody outside can
// write to it. And `List<out E>.contains(element: E)` breaks the rule on
// purpose using `@UnsafeVariance` — contains only *reads* the argument.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Variance answers: "is G<Apple> usable as G<Fruit>?"
//  • `out T` (covariant): T only returned → G<Apple> ⊂ G<Fruit>. E.g. List, Flow.
//  • `in T` (contravariant): T only accepted → G<Fruit> ⊂ G<Apple>. E.g. Comparator.
//  • Neither (invariant): T both in and out → no relationship. E.g. MutableList.
//  • Functions: (in P) -> out R.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// gala, fuji
// basket still holds only apples: [gala]
// §2
// gala is a Fruit
// [fuji]
// §3
// [apple#1, apple#2]
// §4
// [gala, fuji]
// [braeburn, fuji, gala]
// §5
// ripe gala
// §6
// gala
