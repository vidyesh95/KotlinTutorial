/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 08 — Making decisions: `if` and `when`
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : use `if` and `when` as EXPRESSIONS (they produce values), match on
 *         values / ranges / types, and let the compiler prove you covered
 *         every case (exhaustiveness). Meet 🆕 guard conditions.
 *  Needs: lessons 01–07 (variables, types, strings, null safety, collections)
 *  Time : ~30 min
 *  Next : lesson 09 — loops and ranges
 *  Exercises: exercises/ex08/Ex08.kt
 */
package lesson08_if_and_when

fun main() {
    section1IfIsAnExpression()
    section2WhenWithSubject()
    section3WhenWithoutSubject()
    section4ExhaustiveWhen()
    section5GuardConditions()
    section6Calculator()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. `if` is an expression
// ───────────────────────────────────────────────────────────────────────────
// In many languages `if` only *does* things. In Kotlin `if` also *returns* a
// value: the value of the branch that ran. That's why Kotlin has no ternary
// operator (`a > b ? a : b`) — plain `if` already does that job.
//
// 🧒 Like I'm 5:
//    You ask mum "can I have ice cream?". She doesn't just do something — she
//    ANSWERS: "yes" or "no". An `if` expression is a question that answers.
//
// When `if` is used as a value, `else` is REQUIRED (otherwise: no answer when false!).
// In a block `{ ... }` the LAST expression is the block's value.

fun maxOf(a: Int, b: Int): Int = if (a > b) a else b

fun section1IfIsAnExpression() {
    println("§1")
    val a = 10
    val b = 20
    val max = if (a > b) a else b              // like `a > b ? a : b` in Java
    println("max = $max")
    println("max of 7 and 9 is ${maxOf(7, 9)}")

    val temperature = 31
    val advice = if (temperature > 30) {
        val extra = "drink water"                // blocks can do work first…
        "Hot! $extra"                            // …the last line is the value
    } else if (temperature < 5) {
        "Cold! wear a coat"
    } else {
        "Nice weather"
    }
    println(advice)
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `when` with a subject — a super-powered switch
// ───────────────────────────────────────────────────────────────────────────
// `when (x)` compares x against each branch, top to bottom, and runs the FIRST
// branch that matches. No `break` needed, no fall-through bugs.
// A branch condition can be:
//   • one or more values         1, 2 ->
//   • a range check              in 10..19 ->   /  !in 0..9 ->
//   • a type check               is String ->   (smart-casts inside the branch!)
//   • any expression             someFunction(x) ->

fun describe(x: Any): String = when (x) {
    0 -> "zero"
    1, 2, 3 -> "small number"
    in 10..19 -> "teen number"
    is Int -> "some other Int: ${x + 1000}"       // x is smart-cast to Int here
    is String -> "a String of length ${x.length}" // x is smart-cast to String here
    else -> "something else: $x"
}

fun section2WhenWithSubject() {
    println("§2")
    for (thing in listOf(0, 2, 15, 42, "hey", 3.14)) {
        println(describe(thing))
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. `when` without a subject — a tidy if/else-if ladder
// ───────────────────────────────────────────────────────────────────────────
// With no `(x)`, every branch is just a Boolean condition. The first `true` wins.

fun grade(score: Int): Char = when {
    score >= 90 -> 'A'
    score >= 75 -> 'B'
    score >= 50 -> 'C'
    else -> 'F'
}

fun section3WhenWithoutSubject() {
    println("§3")
    println(listOf(95, 80, 51, 12).map(::grade))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Exhaustive `when` — the compiler checks you covered everything
// ───────────────────────────────────────────────────────────────────────────
// When `when` is used as an expression on an enum, a sealed type, or a Boolean,
// the compiler KNOWS all possible values. If you list them all you need no
// `else` — and if someone later adds a new enum entry, your code stops
// compiling until you handle it. That's a feature: bugs become compile errors.
//
// 🧒 Like I'm 5:
//    A teacher with a class list calls every name. If a new kid joins and the
//    teacher forgets them, the headmaster (compiler) shouts before school starts.

enum class TrafficLight { RED, YELLOW, GREEN }

sealed interface Payment
data class Card(val last4: String) : Payment
data class Cash(val amount: Int) : Payment
data object Voucher : Payment

fun action(light: TrafficLight): String = when (light) {
    TrafficLight.RED -> "stop"
    TrafficLight.YELLOW -> "slow down"
    TrafficLight.GREEN -> "go"
    // no else: all 3 entries covered
}

fun describe(payment: Payment): String = when (payment) {
    is Card -> "card ending ${payment.last4}"
    is Cash -> "cash ${payment.amount}"
    Voucher -> "voucher"
}

// 🆕 Kotlin 2.x — data-flow exhaustiveness: the compiler remembers what earlier
// checks already ruled out, so after an early return the `when` below only
// needs to cover what is LEFT.
fun label(payment: Payment): String {
    if (payment is Voucher) return "free"
    return when (payment) {                      // Voucher is already excluded here
        is Card -> "card"
        is Cash -> "cash"
    }
}

fun section4ExhaustiveWhen() {
    println("§4")
    println(TrafficLight.entries.map(::action))
    println(describe(Card("4242")))
    println(describe(Voucher))
    println(label(Voucher) + ", " + label(Cash(5)))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. 🆕 Guard conditions (stable since Kotlin 2.2)
// ───────────────────────────────────────────────────────────────────────────
// A branch can add an extra `if` after its main condition:
//     is Cash if payment.amount > 100 -> ...
// The branch matches only when BOTH hold. Before 2.2 you had to nest another
// `if` inside the branch, which broke exhaustiveness checks.
// (Guards need a `when` WITH a subject.)

fun fee(payment: Payment): String = when (payment) {
    is Cash if payment.amount > 100 -> "big cash: 0 fee, but count it twice"
    is Cash -> "cash: 0 fee"
    is Card if payment.last4.startsWith("9") -> "premium card: 0 fee"
    is Card -> "card: 2% fee"
    Voucher -> "voucher: no fee"
}

fun section5GuardConditions() {
    println("§5")
    listOf(Cash(500), Cash(20), Card("9001"), Card("1234"), Voucher)
        .forEach { println(fee(it)) }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Putting it together: a tiny calculator
// ───────────────────────────────────────────────────────────────────────────
// `when` on a String, returning a value. Note `when` itself is the function body.
// ⚠️ Gotcha: Int division drops the remainder (7 / 2 == 3) and dividing by zero
//    throws ArithmeticException — so we check it first.

fun calculate(a: Int, operator: String, b: Int): String = when (operator) {
    "+" -> "${a + b}"
    "-" -> "${a - b}"
    "*" -> "${a * b}"
    "/" -> if (b == 0) "cannot divide by zero" else "${a / b}"
    else -> "unknown operator '$operator'"
}

fun section6Calculator() {
    println("§6")
    for (line in listOf("7 + 2", "7 / 2", "1 / 0", "2 ^ 8")) {
        val (left, op, right) = line.split(" ")  // destructuring a List into 3 vals
        println("$line = ${calculate(left.toInt(), op, right.toInt())}")
    }
}

// 🏗️ In the real world: `when` over a sealed result type is how you'll turn
// "UserCreated / EmailTaken / InvalidPassword" into HTTP 201 / 409 / 400 in
// Spring controllers — and the compiler makes sure no outcome is forgotten.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `if` and `when` are expressions: they return the value of the chosen branch.
//  • As an expression, `if` needs `else`; `when` needs to be exhaustive.
//  • `when (x)` matches values, `in` ranges, `is` types (with smart casts).
//  • Enums, sealed types and Booleans can be covered fully → no `else` needed.
//  • 🆕 `is X if condition ->` guard conditions refine a branch.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// max = 20
// max of 7 and 9 is 9
// Hot! drink water
// §2
// zero
// small number
// teen number
// some other Int: 1042
// a String of length 3
// something else: 3.14
// §3
// [A, B, C, F]
// §4
// [stop, slow down, go]
// card ending 4242
// voucher
// free, cash
// §5
// big cash: 0 fee, but count it twice
// cash: 0 fee
// premium card: 0 fee
// card: 2% fee
// voucher: no fee
// §6
// 7 + 2 = 9
// 7 / 2 = 3
// 1 / 0 = cannot divide by zero
// 2 ^ 8 = unknown operator '^'
