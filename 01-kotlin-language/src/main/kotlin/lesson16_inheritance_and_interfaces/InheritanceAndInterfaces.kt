/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 16 — Inheritance, abstract classes, interfaces, visibility
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : reuse and specialise behaviour with `open`/`override`, describe
 *         abilities with interfaces, and hide details with visibility modifiers.
 *  Needs: lesson 15 (classes and properties)
 *  Time : ~40 min
 *  Next : lesson 17 — data, enum and value classes
 *  Exercises: exercises/ex16/Ex16.kt
 */
package lesson16_inheritance_and_interfaces

import java.util.Locale

fun main() {
    section1OpenAndOverride()
    section2ConstructorsAndSuper()
    section3AbstractClasses()
    section4Interfaces()
    section5ConflictingDefaults()
    section6Visibility()
    section7CompositionOverInheritance()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Inheritance: `open` and `override`
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Your family recipe for pancakes is the PARENT. You copy it and change one
//    step ("add blueberries") — that's the CHILD. You didn't rewrite the whole
//    recipe, you only overrode one step. But Grandma must first say "you're
//    allowed to change this recipe" — in Kotlin that permission is `open`.
//
// Precisely:
//   • Classes are FINAL by default — nobody can extend them unless marked `open`.
//   • Members are final too; mark them `open` to allow overriding.
//   • A child must write `override` explicitly (no accidental overrides).
//   • Every class ultimately inherits from `Any` (equals, hashCode, toString).

open class Animal(val name: String) {
    open val sound: String = "..."
    open fun speak() = println("$name says $sound")
    fun breathe() = println("$name breathes")     // final: children can't change it
}

class Dog(name: String) : Animal(name) {          // `: Animal(name)` calls the parent constructor
    override val sound = "Woof"
    fun fetch() = println("$name fetches the ball")
}

class Cat(name: String) : Animal(name) {
    override val sound = "Meow"
    override fun speak() {
        super.speak()                              // run the parent version first
        println("$name purrs")
    }
}

fun section1OpenAndOverride() {
    println("§1")
    val pets: List<Animal> = listOf(Dog("Rex"), Cat("Tom"))  // a Dog IS an Animal
    for (pet in pets) pet.speak()                  // each runs ITS OWN speak() — polymorphism
    pets.first().breathe()
    (pets.first() as Dog).fetch()                  // need a cast to use Dog-only functions
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Construction order and `super`
// ───────────────────────────────────────────────────────────────────────────
// The parent is built FIRST, then the child. So the parent's init blocks run
// before the child's.
//
// ⚠️ Gotcha: never call an `open` member from a parent's constructor/init.
//    The child's override would run before the child's own properties exist.

open class Vehicle(val wheels: Int) {
    init {
        println("  Vehicle init: $wheels wheels")
    }
}

class Bike(val brand: String) : Vehicle(wheels = 2) {
    init {
        println("  Bike init: $brand")
    }
}

fun section2ConstructorsAndSuper() {
    println("§2")
    Bike("Brompton")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Abstract classes — a half-finished parent
// ───────────────────────────────────────────────────────────────────────────
// An abstract class can't be created directly. It provides SOME shared code
// and leaves `abstract` members for children to fill in (they're open
// automatically). It can hold state (properties with values) and constructors.

abstract class Shape(val label: String) {
    abstract fun area(): Double                    // no body: every child MUST implement
    fun describe() = "$label with area ${"%.2f".format(Locale.ROOT, area())}"  // shared code using the abstract part
}

class Circle(private val radius: Double) : Shape("circle") {
    override fun area() = Math.PI * radius * radius
}

class Rectangle(private val w: Double, private val h: Double) : Shape("rectangle") {
    override fun area() = w * h
}

fun section3AbstractClasses() {
    println("§3")
    // val s = Shape("blob")                       // ❌ Cannot create an instance of an abstract class
    listOf(Circle(1.0), Rectangle(2.0, 3.0)).forEach { println(it.describe()) }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Interfaces — a list of abilities (a contract)
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A "can swim" badge. A duck, a fish and a submarine all wear it. They are
//    totally different things, but if you need "something that swims", any
//    badge-wearer will do. The badge doesn't care HOW they swim.
//
// Precisely: an interface declares functions/properties a class promises to
// provide. It can have default method bodies but NO stored state.
// A class extends at most ONE class but implements MANY interfaces.

interface Swimmer {
    val maxDepthMeters: Int                        // abstract property: implementer supplies it
    fun swim() = println("${javaClass.simpleName} swims down to $maxDepthMeters m")  // default body
}

interface Flyer {
    fun fly() = println("${javaClass.simpleName} flies")
}

class Duck : Swimmer, Flyer {
    override val maxDepthMeters = 1
}

class Submarine : Swimmer {
    override val maxDepthMeters = 300
    override fun swim() = println("Submarine dives silently to $maxDepthMeters m")
}

fun goSwimming(swimmers: List<Swimmer>) = swimmers.forEach { it.swim() }   // depends on the badge only

fun section4Interfaces() {
    println("§4")
    goSwimming(listOf(Duck(), Submarine()))
    Duck().fly()
}

// 🏗️ In the real world: Spring code is full of interfaces —
//    `interface BookRepository`, `interface PaymentGateway`. Code depends on the
//    interface; tests swap in a fake implementation. That is the heart of
//    dependency injection (module 02).

// ───────────────────────────────────────────────────────────────────────────
// §5. Two interfaces with the same default method
// ───────────────────────────────────────────────────────────────────────────
// If two interfaces both give `onClick()` a body, the class MUST override it
// and may call each one explicitly with `super<Name>.onClick()`.

interface Clickable {
    fun onClick() = println("  Clickable.onClick")
}

interface Focusable {
    fun onClick() = println("  Focusable.onClick")
}

class Button : Clickable, Focusable {
    override fun onClick() {
        super<Clickable>.onClick()
        super<Focusable>.onClick()
        println("  Button.onClick")
    }
}

fun section5ConflictingDefaults() {
    println("§5")
    Button().onClick()
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Visibility modifiers
// ───────────────────────────────────────────────────────────────────────────
//   public    (default) everyone
//   internal  everyone in the same MODULE (here: the whole 01-kotlin-language Gradle module)
//   protected this class and its subclasses (class members only)
//   private   inside this class only; at top level: inside this FILE only
//
// 🧒 Like I'm 5: your house. `public` = front garden, anyone can see.
//    `internal` = the living room, family only. `protected` = your bedroom,
//    you and your kids. `private` = your diary.

open class Base {
    private val diary = "secret"
    protected val bedroom = "kids allowed"
    internal val livingRoom = "family allowed"
    val garden = "everyone"

    fun readDiary() = diary                        // private is fine inside the class
}

class Child : Base() {
    fun tour() = listOf(bedroom, livingRoom, garden)   // diary is NOT visible here
}

fun section6Visibility() {
    println("§6")
    val child = Child()
    println(child.tour())
    println(child.readDiary())
    // child.bedroom                                // ❌ Cannot access 'bedroom': it is protected
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Composition over inheritance
// ───────────────────────────────────────────────────────────────────────────
// Inheritance says "IS-A" and locks a class into one parent forever.
// Composition says "HAS-A": keep a helper object in a property and call it.
// Prefer composition; use inheritance for true "is-a" families.
// (Lesson 21 shows `by`, which makes composition almost free to write.)

interface Engine {
    fun start(): String
}

class ElectricEngine : Engine {
    override fun start() = "hum"
}

class PetrolEngine : Engine {
    override fun start() = "vroom"
}

class Truck(private val engine: Engine) {          // HAS-A engine; swap it freely
    fun drive() = println("Truck goes ${engine.start()}")
}

fun section7CompositionOverInheritance() {
    println("§7")
    Truck(ElectricEngine()).drive()
    Truck(PetrolEngine()).drive()
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Final by default; `open` allows extending/overriding; `override` is mandatory.
//  • Parent constructor runs first: `class Dog(name: String) : Animal(name)`.
//  • Abstract class = shared code + holes (`abstract`); can't be instantiated.
//  • Interface = contract of abilities, defaults allowed, no state, implement many.
//  • Same default in two interfaces → override and pick with `super<X>.f()`.
//  • public / internal (module) / protected (subclasses) / private (class or file).
//  • Prefer composition (has-a) over inheritance (is-a).
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Rex says Woof
// Tom says Meow
// Tom purrs
// Rex breathes
// Rex fetches the ball
// §2
//   Vehicle init: 2 wheels
//   Bike init: Brompton
// §3
// circle with area 3.14
// rectangle with area 6.00
// §4
// Duck swims down to 1 m
// Submarine dives silently to 300 m
// Duck flies
// §5
//   Clickable.onClick
//   Focusable.onClick
//   Button.onClick
// §6
// [kids allowed, family allowed, everyone]
// secret
// §7
// Truck goes hum
// Truck goes vroom
