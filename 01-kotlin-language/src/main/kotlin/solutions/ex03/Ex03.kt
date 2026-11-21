package solutions.ex03

import exercises.ex03.Ex03
import kotlin.math.roundToLong

object Ex03Solution : Ex03 {
    override fun averageOf(a: Int, b: Int): Double = (a.toLong() + b) / 2.0

    override fun isEven(n: Int): Boolean = n % 2 == 0

    override fun roundToCents(amount: Double): Long = (amount * 100).roundToLong()

    override fun hasPermission(flags: Int, permission: Int): Boolean = flags and permission == permission
}
