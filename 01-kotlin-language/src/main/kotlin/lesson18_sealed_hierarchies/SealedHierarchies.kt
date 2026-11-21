/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 18 — Sealed hierarchies
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : model "one of these few shapes" precisely, so the compiler forces
 *         you to handle every case — results, errors and state machines.
 *  Needs: lessons 16–17 (interfaces, data classes, enums)
 *  Time : ~30 min
 *  Next : lesson 19 — objects, companions and nesting
 *  Exercises: exercises/ex18/Ex18.kt
 */
package lesson18_sealed_hierarchies

fun main() {
    section1WhySealed()
    section2ExhaustiveWhen()
    section3ResultsAndErrors()
    section4StateMachine()
    section5SealedVsEnum()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The idea: a closed family of types
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A restaurant with a CLOSED menu: pizza, pasta, salad. That's it. The cook
//    knows every possible order, so the cook can be sure "I know how to make
//    everything somebody might ask for". Nobody can sneak "sushi" onto the menu
//    from outside the kitchen.
//
// Precisely: `sealed interface` / `sealed class` = all direct subtypes must be
// declared in the SAME package and module (usually the same file). The compiler
// therefore knows the complete list of subtypes.
//
// Different from an enum: each subtype is a real class and can carry
// DIFFERENT data (a Card payment has a card number, Cash has nothing).

sealed interface Payment

data class Card(val last4: String) : Payment
data class BankTransfer(val iban: String, val reference: String) : Payment
data object Cash : Payment              // no data → a single object is enough

fun section1WhySealed() {
    println("§1")
    val payments: List<Payment> = listOf(Card("4242"), Cash, BankTransfer("DE89…", "INV-7"))
    payments.forEach(::println)          // data object gets a nice toString too: "Cash"
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Exhaustive `when` — the real superpower
// ───────────────────────────────────────────────────────────────────────────
// Because the list of subtypes is closed, a `when` that covers all of them
// needs NO `else`. Smart casts give you each subtype's data inside the branch.
//
// And here is the magic: add a new subtype tomorrow (say `Crypto`) and every
// `when` that forgot it STOPS COMPILING. The compiler hands you a to-do list.
// With `else ->` you would lose that safety — avoid `else` on sealed types.

fun describe(payment: Payment): String = when (payment) {
    is Card -> "card ending ${payment.last4}"           // smart cast: payment is Card here
    is BankTransfer -> "transfer ref ${payment.reference}"
    Cash -> "cash"                                      // objects are compared directly, no `is`
}

fun fee(payment: Payment): Int = when (payment) {
    is Card -> 30
    is BankTransfer -> 0
    Cash -> 0
}

fun section2ExhaustiveWhen() {
    println("§2")
    for (p in listOf(Card("1111"), BankTransfer("NL91…", "ORDER-9"), Cash)) {
        println("${describe(p)} (fee ${fee(p)})")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Modelling outcomes: success OR a specific failure
// ───────────────────────────────────────────────────────────────────────────
// Instead of throwing exceptions for EXPECTED problems ("email taken"), return
// a value that says what happened. The caller can't forget a case.
//
// 🏗️ In the real world: a sign-up service returns `SignUpResult`, and the
//    REST controller maps each case to an HTTP status (201, 409, 400) — you'll
//    do exactly this in module 06.

sealed interface SignUpResult {
    data class Success(val userId: Int) : SignUpResult
    data class InvalidEmail(val email: String) : SignUpResult
    data object EmailTaken : SignUpResult
    data class WeakPassword(val reasons: List<String>) : SignUpResult
}

val existingEmails = setOf("ada@example.com")

fun signUp(email: String, password: String): SignUpResult {
    if ("@" !in email) return SignUpResult.InvalidEmail(email)
    if (email in existingEmails) return SignUpResult.EmailTaken
    val reasons = buildList {
        if (password.length < 8) add("shorter than 8")
        if (password.none(Char::isDigit)) add("no digit")
    }
    if (reasons.isNotEmpty()) return SignUpResult.WeakPassword(reasons)
    return SignUpResult.Success(userId = 101)
}

fun toHttpStatus(result: SignUpResult): String = when (result) {
    is SignUpResult.Success -> "201 Created (id ${result.userId})"
    is SignUpResult.InvalidEmail -> "400 Bad Request: '${result.email}' is not an email"
    SignUpResult.EmailTaken -> "409 Conflict: email already registered"
    is SignUpResult.WeakPassword -> "400 Bad Request: password ${result.reasons.joinToString()}"
}

fun section3ResultsAndErrors() {
    println("§3")
    println(toHttpStatus(signUp("grace@example.com", "s3cretpass")))
    println(toHttpStatus(signUp("grace.example.com", "s3cretpass")))
    println(toHttpStatus(signUp("ada@example.com", "s3cretpass")))
    println(toHttpStatus(signUp("bob@example.com", "abc")))
}

// ───────────────────────────────────────────────────────────────────────────
// §4. State machines
// ───────────────────────────────────────────────────────────────────────────
// Each state carries ONLY the data that makes sense in that state:
// a Shipped order has a tracking number; a Placed one doesn't — so you can't
// accidentally read a tracking number that doesn't exist yet.
// A sealed CLASS (instead of interface) can hold shared constructor data.

sealed class OrderState(val orderId: Int) {
    class Placed(orderId: Int) : OrderState(orderId)
    class Shipped(orderId: Int, val trackingNo: String) : OrderState(orderId)
    class Delivered(orderId: Int, val signedBy: String) : OrderState(orderId)
    class Cancelled(orderId: Int, val reason: String) : OrderState(orderId)
}

/** Returns the next state, or the same state if the event doesn't apply. */
fun ship(state: OrderState, trackingNo: String): OrderState = when (state) {
    is OrderState.Placed -> OrderState.Shipped(state.orderId, trackingNo)
    is OrderState.Shipped, is OrderState.Delivered, is OrderState.Cancelled -> state   // several types, one branch
}

fun status(state: OrderState): String = when (state) {
    is OrderState.Placed -> "#${state.orderId} placed"
    is OrderState.Shipped -> "#${state.orderId} shipped, tracking ${state.trackingNo}"
    is OrderState.Delivered -> "#${state.orderId} delivered, signed by ${state.signedBy}"
    is OrderState.Cancelled -> "#${state.orderId} cancelled: ${state.reason}"
}

fun section4StateMachine() {
    println("§4")
    var order: OrderState = OrderState.Placed(7)
    println(status(order))
    order = ship(order, "TRK-123")
    println(status(order))
    order = ship(order, "TRK-999")                       // already shipped → unchanged
    println(status(order))
    println(status(OrderState.Cancelled(8, "out of stock")))
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Sealed or enum?
// ───────────────────────────────────────────────────────────────────────────
//   enum   → every case has the SAME shape (maybe the same few properties).
//            Light colours, roles, currencies. Easy to list, store and parse.
//   sealed → cases carry DIFFERENT data. Results, errors, events, states.
//
// ⚠️ Gotcha: `data object` vs plain `object` — prefer `data object` inside a
//    sealed hierarchy: you get a readable toString ("Cash" instead of
//    "Cash@1b6d3586") and a correct equals/hashCode.

object PlainNothing
data object DataNothing

fun section5SealedVsEnum() {
    println("§5")
    println(DataNothing)
    println(PlainNothing.toString().startsWith("lesson18_sealed_hierarchies.PlainNothing@"))
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `sealed interface/class` = closed family; subtypes live in the same package+module.
//  • `when` over a sealed type needs no `else` and breaks loudly when a new case appears.
//  • Each subtype carries its own data; smart casts unlock it inside the branch.
//  • Great for results/errors (map to HTTP codes), events and state machines.
//  • Use `data object` for cases without data; use enum when all cases look alike.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Card(last4=4242)
// Cash
// BankTransfer(iban=DE89…, reference=INV-7)
// §2
// card ending 1111 (fee 30)
// transfer ref ORDER-9 (fee 0)
// cash (fee 0)
// §3
// 201 Created (id 101)
// 400 Bad Request: 'grace.example.com' is not an email
// 409 Conflict: email already registered
// 400 Bad Request: password shorter than 8, no digit
// §4
// #7 placed
// #7 shipped, tracking TRK-123
// #7 shipped, tracking TRK-123
// #8 cancelled: out of stock
// §5
// DataNothing
// true
