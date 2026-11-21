package solutions.ex11

import exercises.ex11.Ex11
import java.util.Locale

object Ex11Solution : Ex11 {
    override fun formatPrice(amount: Double, currency: String, decimals: Int): String =
        "%.${decimals}f".format(Locale.ROOT, amount) + " " + currency

    override fun maxOfAll(vararg numbers: Int): Int? = numbers.maxOrNull()

    override fun power(base: Long, exponent: Int): Long =
        if (exponent == 0) 1 else base * power(base, exponent - 1)

    override fun fibonacci(n: Int): Long = fib(n, 0, 1)

    private tailrec fun fib(n: Int, current: Long, next: Long): Long =
        if (n == 0) current else fib(n - 1, next, current + next)
}
