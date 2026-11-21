package solutions.ex15

import exercises.ex15.BankAccount
import exercises.ex15.Ex15
import exercises.ex15.Temperature

object Ex15Solution : Ex15 {
    override fun openAccount(owner: String): BankAccount = SimpleAccount(owner)
    override fun temperature(celsius: Double): Temperature = SimpleTemperature(celsius)

    override fun cleanUsername(raw: String): String = raw.trim().lowercase().ifEmpty { "anonymous" }
}

class SimpleAccount(override val owner: String) : BankAccount {
    override var balance: Long = 0
        private set

    init {
        require(owner.isNotBlank()) { "owner must not be blank" }
    }

    override fun deposit(amount: Long) {
        require(amount > 0) { "amount must be positive" }
        balance += amount
    }

    override fun withdraw(amount: Long): Boolean {
        if (amount <= 0 || amount > balance) return false
        balance -= amount
        return true
    }
}

class SimpleTemperature(override var celsius: Double) : Temperature {
    override var fahrenheit: Double
        get() = celsius * 9 / 5 + 32
        set(value) {
            celsius = (value - 32) * 5 / 9
        }
}
