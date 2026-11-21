/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 07 — Collections: arrays, lists, sets, maps and sequences
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : pick the right collection, know read-only vs mutable, build
 *         collections, and transform data with map/filter/groupBy/fold & co.
 *         Finally, see how sequences process data lazily.
 *  Needs: lessons 02–06 (lambdas `{ it ... }` are explained fully in lesson 12;
 *         for now read `{ it.price }` as "for each element, take its price")
 *  Time : ~45 min
 *  Next : lesson 08 — if and when
 *  Exercises: exercises/ex07/Ex07.kt
 */
package lesson07_collections

fun main() {
    section1Arrays()
    section2Lists()
    section3ReadOnlyVsMutable()
    section4Sets()
    section5Maps()
    section6Transformations()
    section7GroupingAndAggregating()
    section8Sequences()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Arrays — fixed size
// ───────────────────────────────────────────────────────────────────────────
// An Array has a FIXED length chosen at creation; elements can be replaced.
// `IntArray`, `DoubleArray`... are primitive arrays (no boxing, faster).
// In everyday Kotlin you'll use List far more often — arrays are for
// performance-sensitive code, `main(args)`, and Java interop.
//
// ⚠️ Gotcha: `println(array)` prints something like `[I@1b6d3586` (a memory
//    address). Use `array.contentToString()`. Same for `==`: arrays compare by
//    identity, use `contentEquals`.

fun section1Arrays() {
    println("§1")
    val arr = arrayOf(1, 2, 3)
    arr[0] = 10
    println(arr.contentToString())
    val zeros = IntArray(3)                  // [0, 0, 0]
    val squares = IntArray(4) { i -> i * i } // build from index
    println("${zeros.contentToString()} ${squares.contentToString()} size=${squares.size}")
    println(arrayOf(1, 2).contentEquals(arrayOf(1, 2)))
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Lists — ordered, duplicates allowed
// ───────────────────────────────────────────────────────────────────────────
// The workhorse. Indexed from 0, keeps insertion order, may contain duplicates.

fun section2Lists() {
    println("§2")
    val fruits = listOf("banana", "avocado", "apple", "orange")
    println("${fruits[1]} ${fruits.size} ${fruits.first()} ${fruits.last()}")
    println("${"apple" in fruits} ${fruits.indexOf("apple")}")
    println(fruits.filter { it.startsWith("a") }.sorted().map { it.uppercase() })

    val numbers = buildList {               // build a read-only list step by step
        add(1)
        addAll(listOf(2, 3))
        if (size < 5) add(99)
    }
    println(numbers)
    for ((index, fruit) in fruits.withIndex()) {
        if (index < 2) println("$index: $fruit")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Read-only vs mutable
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    `List` is a museum display case: you can LOOK at everything, but you
//    can't touch. `MutableList` is your toy box: you can add, remove and swap
//    toys. Every collection type comes in both flavours:
//       List / MutableList,  Set / MutableSet,  Map / MutableMap.
//
// Default to read-only (`listOf`). Use mutable (`mutableListOf`) locally while
// building something, then hand out the read-only type.
//
// ⚠️ Gotcha: read-only is NOT the same as immutable. A `List` view of a
//    MutableList changes when the underlying list changes — you just can't
//    change it THROUGH the `List` reference. (Same as the glass case: the
//    museum staff can still rearrange things behind it.)

fun section3ReadOnlyVsMutable() {
    println("§3")
    val cart = mutableListOf("pen")
    cart.add("book")
    cart += "mug"                            // operator form of add
    cart.remove("pen")
    cart[0] = "notebook"                     // replace by index
    println(cart)

    val view: List<String> = cart            // read-only VIEW of the same list
    // view.add("x")                         // ❌ Unresolved reference: List has no add()
    cart.add("lamp")
    println(view)                            // sees the change!
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Sets — no duplicates
// ───────────────────────────────────────────────────────────────────────────
// A Set holds each element at most once. `contains` is very fast.
// `setOf` keeps insertion order (it's a LinkedHashSet under the hood).
// Set maths: union, intersect, subtract.

fun section4Sets() {
    println("§4")
    val tags = setOf("kotlin", "jvm", "kotlin", "backend")
    println("$tags size=${tags.size}")
    val seen = mutableSetOf<Int>()
    println("${seen.add(1)} ${seen.add(1)}")  // second add returns false: already there
    val a = setOf(1, 2, 3)
    val b = setOf(2, 3, 4)
    println("${a union b} ${a intersect b} ${a subtract b}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Maps — look up a value by its key
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A map is a coat-check: you hand over a ticket number (KEY) and get back
//    exactly your coat (VALUE). Each ticket number is unique.
//
// `"one" to 1` creates a Pair(key, value). Looking up a missing key gives null.
// 🏗️ In the real world: JSON objects, HTTP headers, caches, "id → entity" indexes.

fun section5Maps() {
    println("§5")
    val stock = mapOf("apple" to 3, "pear" to 0)
    println(stock)
    println("${stock["apple"]} ${stock["kiwi"]} ${stock.getOrDefault("kiwi", 0)}")
    println("${stock.keys} ${stock.values} ${"pear" in stock}")

    val prices = mutableMapOf<String, Int>()
    prices["coffee"] = 3                     // put
    prices["tea"] = 2
    prices["coffee"] = 4                     // overwrite
    prices.remove("tea")
    for ((item, price) in prices) println("$item costs $price")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Transformations — the everyday toolbox
// ───────────────────────────────────────────────────────────────────────────
// These functions never change the original; they return a NEW collection.
// Think "describe WHAT you want", not "write a loop that does it".

data class Order(val customer: String, val item: String, val price: Int)

val orders = listOf(
    Order("ann", "book", 12),
    Order("bob", "pen", 2),
    Order("ann", "lamp", 30),
    Order("cid", "book", 12),
    Order("bob", "mug", 8),
)

fun section6Transformations() {
    println("§6")
    println(orders.map { it.price })                              // transform each
    println(orders.filter { it.price > 10 }.map { it.item })      // keep some
    println(orders.sortedBy { it.price }.map { it.item })         // sort (stable)
    println(orders.sortedByDescending { it.price }.take(2).map { it.item })
    println(orders.map { it.item }.distinct())
    println("${orders.any { it.price > 25 }} ${orders.all { it.price > 1 }} ${orders.none { it.price > 100 }}")
    println("${orders.count { it.customer == "ann" }} ${orders.maxBy { it.price }.item}")
    println(listOf("a", "b", "c").zip(listOf(1, 2)))              // pair up; shortest wins
    println(listOf(1, 2, 3, 4).windowed(2))                       // sliding window
    println((1..5).chunked(2))                                    // fixed-size pieces
    // 🆕 Kotlin 2.4: isSorted() & friends are stable
    println("${listOf(1, 2, 3).isSorted()} ${orders.map { it.price }.isSorted()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Grouping and aggregating
// ───────────────────────────────────────────────────────────────────────────
//   sumOf / average / fold / reduce  → collapse many values into one
//   groupBy        → Map<Key, List<Element>>
//   associateBy    → Map<Key, Element> (last one wins on duplicate keys)
//   partition      → Pair(matching, notMatching)
//
// `fold(start) { acc, x -> ... }`: 🧒 a snowball rolling down a hill. It starts
// as `start` and grows a little with every element it rolls over.
//
// 🏗️ In the real world: this is exactly what SQL's SUM/GROUP BY do (module 03).
//    Rule: aggregate in the DATABASE when data is big; in Kotlin when it's
//    already in memory.

fun section7GroupingAndAggregating() {
    println("§7")
    val prices = orders.map { it.price }
    println("sum=${orders.sumOf { it.price }} avg=${prices.average()}")
    println("fold=${prices.fold(0) { acc, p -> acc + p }} reduce-max=${prices.reduce { a, b -> maxOf(a, b) }}")

    val spentPerCustomer = orders.groupBy { it.customer }.mapValues { (_, list) -> list.sumOf { it.price } }
    println(spentPerCustomer)
    println(orders.associateBy { it.item }.keys)
    val (expensive, cheap) = orders.partition { it.price >= 10 }
    println("expensive=${expensive.size} cheap=${cheap.size}")
}

// ───────────────────────────────────────────────────────────────────────────
// §8. Sequences — lazy assembly lines
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A LIST pipeline is like baking: first mix ALL the dough, then shape ALL
//    the cookies, then bake ALL of them. A SEQUENCE is an assembly line: ONE
//    cookie goes through mix → shape → bake, then the next one. If you only
//    need the first good cookie, you stop early and save lots of work.
//
// `asSequence()` makes the steps lazy; a terminal operation (toList, first,
// sum...) starts the line. Use sequences for long chains on big data or when
// you stop early; for small lists plain collections are simpler and fast.

fun section8Sequences() {
    println("§8")
    println("-- list (eager):")
    val fromList = listOf(1, 2, 3).map { println("map $it"); it * 10 }.first { it > 15 }
    println("result $fromList")

    println("-- sequence (lazy):")
    val fromSeq = listOf(1, 2, 3).asSequence().map { println("map $it"); it * 10 }.first { it > 15 }
    println("result $fromSeq")

    println(generateSequence(1) { it * 2 }.take(5).toList())     // infinite, but we only take 5
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • List (ordered), Set (unique), Map (key → value); Array = fixed size.
//  • Read-only by default (listOf); mutable when building (mutableListOf).
//  • map / filter / sortedBy / groupBy / fold / partition return NEW collections.
//  • Sequences are lazy: element-by-element, can stop early.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// [10, 2, 3]
// [0, 0, 0] [0, 1, 4, 9] size=4
// true
// §2
// avocado 4 banana orange
// true 2
// [APPLE, AVOCADO]
// [1, 2, 3, 99]
// 0: banana
// 1: avocado
// §3
// [notebook, mug]
// [notebook, mug, lamp]
// §4
// [kotlin, jvm, backend] size=3
// true false
// [1, 2, 3, 4] [2, 3] [1]
// §5
// {apple=3, pear=0}
// 3 null 0
// [apple, pear] [3, 0] true
// coffee costs 4
// §6
// [12, 2, 30, 12, 8]
// [book, lamp, book]
// [pen, mug, book, book, lamp]
// [lamp, book]
// [book, pen, lamp, mug]
// true true true
// 2 lamp
// [(a, 1), (b, 2)]
// [[1, 2], [2, 3], [3, 4]]
// [[1, 2], [3, 4], [5]]
// true false
// §7
// sum=64 avg=12.8
// fold=64 reduce-max=30
// {ann=42, bob=10, cid=12}
// [book, pen, lamp, mug]
// expensive=3 cheap=2
// §8
// -- list (eager):
// map 1
// map 2
// map 3
// result 20
// -- sequence (lazy):
// map 1
// map 2
// result 20
// [1, 2, 4, 8, 16]
