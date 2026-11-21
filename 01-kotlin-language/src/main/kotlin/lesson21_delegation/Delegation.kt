/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 21 — Delegation: `by` for classes and properties
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : hand work to another object with one keyword — whole interfaces
 *         (class delegation) or single properties (lazy, observable,
 *         vetoable, map-backed, and your own delegates).
 *  Needs: lessons 15–20 (interfaces, composition, operators)
 *  Time : ~35 min
 *  Next : lesson 22 — the type system
 *  Exercises: exercises/ex21/Ex21.kt
 */
package lesson21_delegation

import kotlin.properties.Delegates
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun main() {
    section1ClassDelegation()
    section2DecoratorWithBy()
    section3Lazy()
    section4ObservableAndVetoable()
    section5MapBackedProperties()
    section6CustomDelegates()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Class delegation: "implement this interface by forwarding to that object"
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    You're asked to do the dishes AND walk the dog. Your sister is great at
//    dishes, your brother loves dogs. You say "dishes — by my sister, dog — by
//    my brother". Grown-ups still ask YOU, but the work is done by them.
//
// `class MediaFile(d: Downloader, p: Player) : Downloader by d, Player by p`
// The compiler writes every forwarding method for you. This is composition
// (lesson 16 §7) with zero boilerplate.

interface Downloader {
    fun download(): String
}

interface Player {
    fun play(): String
}

class FileDownloader(private val file: String) : Downloader {
    override fun download() = "$file downloaded"
}

class FilePlayer(private val file: String) : Player {
    override fun play() = "$file playing"
}

class MediaFile(downloader: Downloader, player: Player) : Downloader by downloader, Player by player

fun section1ClassDelegation() {
    println("§1")
    val file = "song.mp3"
    val media = MediaFile(FileDownloader(file), FilePlayer(file))
    println(media.download())
    println(media.play())
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Decorators: forward everything, override just one thing
// ───────────────────────────────────────────────────────────────────────────
// Delegate the whole interface, then override only the member you want to
// change. Classic use: add logging/counting around an existing object.
//
// ⚠️ Gotcha: the delegate does NOT know about your override. If the delegate
//    internally calls its own `add`, it calls ITS version, not yours.

class CountingSet<T>(private val inner: MutableSet<T> = HashSet()) : MutableSet<T> by inner {
    var attempts = 0
        private set

    override fun add(element: T): Boolean {
        attempts++
        return inner.add(element)
    }
}

fun section2DecoratorWithBy() {
    println("§2")
    val tags = CountingSet<String>()
    listOf("kotlin", "spring", "kotlin", "sql").forEach { tags.add(it) }
    println("attempts=${tags.attempts}, size=${tags.size}, contains sql=${"sql" in tags}")
}

// 🏗️ In the real world: Spring builds "proxies" around your beans to add
//    transactions (@Transactional) and security (@PreAuthorize). Same idea:
//    an object that forwards to yours and adds behaviour around the call.

// ───────────────────────────────────────────────────────────────────────────
// §3. Property delegation and `lazy`
// ───────────────────────────────────────────────────────────────────────────
// `val x: T by something` means: "whenever x is read, ask `something`".
// The delegate object owns get (and set, for var). Write the logic once,
// reuse it on any property.
//
// `by lazy { … }` computes the value on FIRST access, then remembers it.
// 🧒 Like I'm 5: you don't cook dinner until someone is actually hungry —
//    and once it's cooked, everyone eats from the same pot.
//
// Thread-safety modes:
//   lazy { }                                    SYNCHRONIZED (default): computed once, even with many threads
//   lazy(LazyThreadSafetyMode.PUBLICATION) { }  several threads may compute, first result wins
//   lazy(LazyThreadSafetyMode.NONE) { }         no locking — only when one thread uses it

class Report(private val rows: List<Int>) {
    val total: Int by lazy {
        println("  (computing total…)")
        rows.sum()
    }
}

fun section3Lazy() {
    println("§3")
    val report = Report(listOf(10, 20, 30))
    println("report created")
    println("total=${report.total}")
    println("total=${report.total}")                // no recomputation
}

// ───────────────────────────────────────────────────────────────────────────
// §4. observable and vetoable
// ───────────────────────────────────────────────────────────────────────────
// `Delegates.observable(initial) { prop, old, new -> … }` runs AFTER every change.
// `Delegates.vetoable(initial) { prop, old, new -> Boolean }` runs BEFORE; return
// false to reject the change.

class Thermostat {
    var target: Int by Delegates.observable(20) { property, old, new ->
        println("  ${property.name}: $old -> $new")
    }

    var maxPower: Int by Delegates.vetoable(50) { _, _, new -> new in 0..100 }
}

fun section4ObservableAndVetoable() {
    println("§4")
    val t = Thermostat()
    t.target = 22
    t.target = 19
    t.maxPower = 80
    t.maxPower = 150                                 // vetoed: stays 80
    println("maxPower=${t.maxPower}")
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Properties backed by a map
// ───────────────────────────────────────────────────────────────────────────
// A `Map<String, Any?>` can act as the delegate: the property NAME is used
// as the key. Handy for dynamic data such as parsed JSON or config.
// ⚠️ A missing key throws NoSuchElementException on read.

class UserSettings(map: Map<String, Any?>) {
    val theme: String by map
    val fontSize: Int by map
}

fun section5MapBackedProperties() {
    println("§5")
    val settings = UserSettings(mapOf("theme" to "dark", "fontSize" to 14))
    println("theme=${settings.theme}, fontSize=${settings.fontSize}")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Writing your own delegate
// ───────────────────────────────────────────────────────────────────────────
// A delegate is any object with `operator fun getValue(thisRef, property)`
// (and `setValue(thisRef, property, value)` for var). Implementing the
// interfaces ReadOnlyProperty / ReadWriteProperty gives you the right
// signatures for free.
//
// `provideDelegate` (advanced) runs once when the property is CREATED, e.g. to
// validate the property name. Below, `trimmed()` uses it to register names.

class Trimmed(private val minLength: Int) : ReadWriteProperty<Any?, String> {
    private var value = ""

    override fun getValue(thisRef: Any?, property: KProperty<*>): String = value

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String) {
        val clean = value.trim()
        if (clean.length >= minLength) this.value = clean    // ignore too-short input
    }
}

/** A read-only delegate built from a lambda: always returns the property's own name in upper case. */
fun nameShouter(): ReadOnlyProperty<Any?, String> = ReadOnlyProperty { _, property -> property.name.uppercase() }

val registeredFields = mutableListOf<String>()

class Registering {
    operator fun provideDelegate(thisRef: Any?, property: KProperty<*>): ReadWriteProperty<Any?, String> {
        registeredFields += property.name                    // runs when the property is created
        return Trimmed(minLength = 1)
    }
}

class Student {
    var firstName: String by Trimmed(minLength = 2)
    var lastName: String by Registering()
    val greeting: String by nameShouter()
}

fun section6CustomDelegates() {
    println("§6")
    val student = Student()
    student.firstName = "  Vidyesh "
    student.firstName = "X"                                  // too short → ignored
    student.lastName = " Churi  "
    println("'${student.firstName}' '${student.lastName}'")
    println(student.greeting)
    println("registered: $registeredFields")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • `class A(b: B) : B by b` forwards every member of interface B to b (composition, no boilerplate).
//  • Override one member to build a decorator; the delegate won't see your override.
//  • `val x by lazy { }` = compute on first use, cache forever (thread-safe by default).
//  • `Delegates.observable` reacts after a change; `vetoable` can reject it.
//  • `by map` reads properties from a Map by name.
//  • Own delegate: getValue/setValue operators, or ReadOnlyProperty/ReadWriteProperty.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// song.mp3 downloaded
// song.mp3 playing
// §2
// attempts=4, size=3, contains sql=true
// §3
// report created
//   (computing total…)
// total=60
// total=60
// §4
//   target: 20 -> 22
//   target: 22 -> 19
// maxPower=80
// §5
// theme=dark, fontSize=14
// §6
// 'Vidyesh' 'Churi'
// GREETING
// registered: [lastName]
