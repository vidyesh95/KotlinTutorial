package exercises.ex15

/*
 * Exercise 15 — Classes and properties          (lesson: lesson15_classes_and_properties)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex15/Ex15.kt.
 */

/** A bank account whose balance can be READ by anyone but only changed through deposit/withdraw. */
interface BankAccount {
    val owner: String
    val balance: Long

    /** Adds [amount] (must be > 0, otherwise throw IllegalArgumentException). */
    fun deposit(amount: Long)

    /** Removes [amount] if enough money is there and returns true; otherwise changes nothing and returns false. */
    fun withdraw(amount: Long): Boolean
}

/** A temperature stored in Celsius that can also be read AND written in Fahrenheit. */
interface Temperature {
    var celsius: Double

    /** Computed from [celsius]: F = C * 9 / 5 + 32. Setting it updates [celsius]. */
    var fahrenheit: Double
}

interface Ex15 {
    /**
     * 15.1 — Create an account. Throw IllegalArgumentException if [owner] is blank.
     * Hint: write `class SimpleAccount(...) : BankAccount` with `override var balance … private set`
     * and an `init { require(...) }` block.
     */
    fun openAccount(owner: String): BankAccount

    /** 15.2 — Create a [Temperature] starting at [celsius]. Hint: a custom getter/setter pair on `fahrenheit`. */
    fun temperature(celsius: Double): Temperature

    /**
     * 15.3 — Return a username cleaned the way a setter would: trimmed and lowercased.
     * If the result is empty, return "anonymous".
     */
    fun cleanUsername(raw: String): String
}

object Ex15Exercise : Ex15 {
    override fun openAccount(owner: String): BankAccount = TODO("15.1")
    override fun temperature(celsius: Double): Temperature = TODO("15.2")
    override fun cleanUsername(raw: String): String = TODO("15.3")
}
