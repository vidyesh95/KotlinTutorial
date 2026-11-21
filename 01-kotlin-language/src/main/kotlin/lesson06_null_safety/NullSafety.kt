/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 06 — Null safety: the "billion-dollar mistake", fixed
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : know nullable (`String?`) vs non-null (`String`) types and handle
 *         null with ?. ?: !! let, smart casts and as?, also inside collections.
 *  Needs: lessons 02–05
 *  Time : ~35 min
 *  Next : lesson 07 — collections
 *  Exercises: exercises/ex06/Ex06.kt
 */
package lesson06_null_safety

fun main() {
    section1NullableTypes()
    section2SafeCall()
    section3Elvis()
    section4SmartCasts()
    section5Let()
    section6NotNullAssertion()
    section7SafeCast()
    section8NullsInCollections()
    section9LateinitPreview()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Nullable and non-null types
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Imagine gift boxes. A `String` box ALWAYS has a present inside.
//    A `String?` box (note the question mark) MIGHT be empty — that "empty"
//    is called `null`. Before you play with the present from a `?` box, you
//    must check whether there's one inside. Kotlin won't let you forget.
//
// In Java (and many languages) ANY object can be null, and using one crashes
// with NullPointerException (NPE). Its inventor called null his "billion-dollar
// mistake". Kotlin puts "can it be null?" INTO THE TYPE, so the compiler
// catches the mistake before the program ever runs.

fun section1NullableTypes() {
    println("§1")
    var name: String = "Ada"
    // name = null                       // ❌ Null can not be a value of a non-null type String
    var nickname: String? = "Countess"
    println("${name.length} $nickname")
    nickname = null                      // ✅ allowed: the type has a `?`
    println(nickname)
    // println(nickname.length)          // ❌ Only safe (?.) or non-null asserted (!!.) calls are allowed
    name += "!"
    println(name)
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Safe call  ?.
// ───────────────────────────────────────────────────────────────────────────
// `x?.foo()` means: "if x is not null, call foo(); otherwise the whole thing
// is null". The result type is nullable. Calls can be CHAINED: if any link
// is null, the chain stops and gives null.

data class Address(val city: String?)
data class Customer(val name: String, val address: Address?)

fun section2SafeCall() {
    println("§2")
    var favoriteActor: String? = "Sandra Oh"
    println(favoriteActor?.length)
    favoriteActor = null
    println(favoriteActor?.length)

    val withCity = Customer("Ann", Address("Oslo"))
    val noAddress = Customer("Bob", null)
    println(withCity.address?.city?.uppercase())
    println(noAddress.address?.city?.uppercase())
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Elvis operator  ?:
// ───────────────────────────────────────────────────────────────────────────
// `a ?: b` means "a, or if a is null, b". (Tilt your head: ?: looks like
// Elvis Presley's hair.) The right side may also be `return`, `throw` or
// `break` — a neat way to exit early.

fun lengthOrZero(s: String?): Int = s?.length ?: 0

fun requireName(name: String?): String {
    val clean = name ?: return "anonymous"     // early return on null
    return clean.trim()
}

fun section3Elvis() {
    println("§3")
    println(lengthOrZero("hello"))
    println(lengthOrZero(null))
    println(requireName("  Grace "))
    println(requireName(null))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Smart casts — check once, then use freely
// ───────────────────────────────────────────────────────────────────────────
// After `if (x != null)`, inside that branch Kotlin KNOWS x isn't null and
// treats it as the non-null type automatically ("smart cast").
// Works for local `val`s and local `var`s not changed in between.

fun printProduct(arg1: String, arg2: String) {
    val x = arg1.toIntOrNull()
    val y = arg2.toIntOrNull()
    if (x == null || y == null) {
        println("'$arg1' or '$arg2' is not a number")
        return
    }
    println(x * y)                             // x and y are smart-cast to Int here
}

fun section4SmartCasts() {
    println("§4")
    printProduct("6", "7")
    printProduct("a", "7")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. `let` — run a block only when not null
// ───────────────────────────────────────────────────────────────────────────
// `x?.let { ... }` runs the block only if x is not null; inside, `it` is the
// non-null value. Very common for "do something if present".

fun section5Let() {
    println("§5")
    val email: String? = "ada@example.com"
    email?.let { println("Sending mail to $it") }
    val missing: String? = null
    missing?.let { println("never printed") }

    val x: Int? = 3
    val y: Int? = 2
    val sum = x?.let { a -> y?.let { b -> a + b } }   // both present → 5
    println(sum)
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Not-null assertion  !!
// ───────────────────────────────────────────────────────────────────────────
// `x!!` says "trust me, it's not null" and converts to the non-null type.
// If you're wrong, it throws NullPointerException right there.
//
// ⚠️ Gotcha: `!!` is a code smell. Each `!!` is a place your program may crash.
//    Prefer ?. ?: let or an explicit check with a clear error:
//    `val id = maybeId ?: error("id missing")` (lesson 10).

fun section6NotNullAssertion() {
    println("§6")
    val config: String? = "debug=true"
    val sure: String = config!!                // fine, because it isn't null
    println(sure.length)
    val result = runCatching { val nothing: String? = null; nothing!!.length }
    println("!! on null threw ${result.exceptionOrNull()?.javaClass?.simpleName}")
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Safe cast  as?
// ───────────────────────────────────────────────────────────────────────────
// `x as String` crashes if x isn't a String. `x as? String` gives null instead.
// Combine with Elvis for a default.

fun section7SafeCast() {
    println("§7")
    val values: List<Any> = listOf("text", 42)
    for (v in values) {
        val text = v as? String
        println(text?.uppercase() ?: "not a string: $v")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §8. Null inside collections
// ───────────────────────────────────────────────────────────────────────────
//   List<Int?>   → the LIST exists, but elements may be null
//   List<Int>?   → elements are never null, but the LIST itself may be null
// Helpers: filterNotNull(), mapNotNull { }, firstOrNull(), getOrNull(i).

fun section8NullsInCollections() {
    println("§8")
    val readings: List<Int?> = listOf(1, 2, null, 4)
    println(readings.filterNotNull())
    println(listOf("1", "x", "3").mapNotNull { it.toIntOrNull() })
    println(emptyList<Int>().firstOrNull())
    println(listOf(10, 20).getOrNull(5) ?: -1)
    val maybeList: List<String>? = null
    println(maybeList.orEmpty().size)
}

// ───────────────────────────────────────────────────────────────────────────
// §9. Preview: lateinit
// ───────────────────────────────────────────────────────────────────────────
// Sometimes a non-null property can't be set in the constructor, but WILL be
// set before use (test setup, frameworks). `lateinit var` avoids `?`:
// reading it before assignment throws UninitializedPropertyAccessException.
// 🏗️ In the real world: older Spring code used `@Autowired lateinit var repo`.
//    Prefer constructor injection (module 02) — then you need neither ? nor lateinit.

class Greeter {
    lateinit var greeting: String
    fun greet(name: String) = "$greeting, $name"
}

fun section9LateinitPreview() {
    println("§9")
    val greeter = Greeter()
    greeter.greeting = "Howdy"
    println(greeter.greet("Ada"))
}

// 🏗️ Platform types: values coming from JAVA code have unknown nullability,
//    shown as `String!` in the IDE. Kotlin trusts you; decide yourself whether
//    to treat them as `String` or `String?`. When unsure, choose `String?`.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `T?` may hold null, `T` never does — checked at compile time.
//  • `?.` safe call, `?:` default/early exit, `?.let { }` do-if-present.
//  • `if (x != null)` smart-casts x to non-null inside the branch.
//  • `!!` crashes on null — avoid. `as?` casts or gives null.
//  • filterNotNull / mapNotNull / firstOrNull / orEmpty for collections.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 3 Countess
// null
// Ada!
// §2
// 9
// null
// OSLO
// null
// §3
// 5
// 0
// Grace
// anonymous
// §4
// 42
// 'a' or '7' is not a number
// §5
// Sending mail to ada@example.com
// 5
// §6
// 10
// !! on null threw NullPointerException
// §7
// TEXT
// not a string: 42
// §8
// [1, 2, 4]
// [1, 3]
// null
// -1
// 0
// §9
// Howdy, Ada
