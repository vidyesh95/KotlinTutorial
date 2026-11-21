package exercises.ex16

/*
 * Exercise 16 — Inheritance and interfaces          (lesson: lesson16_inheritance_and_interfaces)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex16/Ex16.kt.
 */

/** Shared parent for every kind of employee. Subclasses only decide how pay is calculated. */
abstract class Employee(val name: String) {
    abstract fun monthlyPay(): Int
    fun payslip() = "$name: ${monthlyPay()}"
}

interface Greeter {
    val name: String
    fun greet() = "Hello, I am $name"
}

interface Walker {
    fun walk(steps: Int) = "walked $steps steps"
}

interface Ex16 {
    /** 16.1 — An employee paid [yearlySalary] per year: monthlyPay = yearlySalary / 12 (integer division). */
    fun salaried(name: String, yearlySalary: Int): Employee

    /**
     * 16.2 — An employee paid per hour: hours * rate, but every hour ABOVE 160 is paid 1.5x
     * (round down). Example: 170 h at 10 → 160*10 + 10*15 = 1750.
     */
    fun hourly(name: String, hours: Int, rate: Int): Employee

    /** 16.3 — Sum of monthlyPay() of everybody. Works for ANY Employee subclass (polymorphism). */
    fun payroll(staff: List<Employee>): Int

    /**
     * 16.4 — Return an object that is BOTH a [Greeter] and a [Walker].
     * Its walk() must be overridden to say "<name> walked <steps> steps".
     */
    fun robot(name: String): Greeter
}

object Ex16Exercise : Ex16 {
    override fun salaried(name: String, yearlySalary: Int): Employee = TODO("16.1")
    override fun hourly(name: String, hours: Int, rate: Int): Employee = TODO("16.2")
    override fun payroll(staff: List<Employee>): Int = TODO("16.3")
    override fun robot(name: String): Greeter = TODO("16.4")
}
