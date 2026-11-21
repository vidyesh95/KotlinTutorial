package solutions.ex10

import exercises.ex10.Ex10
import exercises.ex10.InsufficientFundsException

object Ex10Solution : Ex10 {
    override fun safeDivide(a: Int, b: Int): Int? = try {
        a / b
    } catch (e: ArithmeticException) {
        null
    }

    override fun parseAge(text: String): Int {
        val age = requireNotNull(text.trim().toIntOrNull()) { "'$text' is not a number" }
        require(age in 0..150) { "age $age out of range" }
        return age
    }

    override fun parseAll(texts: List<String>): Result<List<Int>> = runCatching { texts.map { it.toInt() } }

    override fun withdraw(balance: Int, amount: Int): Int {
        require(amount > 0) { "amount must be positive" }
        if (amount > balance) throw InsufficientFundsException(amount - balance)
        return balance - amount
    }
}
