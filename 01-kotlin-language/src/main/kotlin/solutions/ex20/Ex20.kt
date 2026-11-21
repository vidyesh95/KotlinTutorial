package solutions.ex20

import exercises.ex20.Ex20
import exercises.ex20.Fraction

fun String.capitalizeWords(): String = split(" ").joinToString(" ") { word -> word.replaceFirstChar(Char::uppercaseChar) }

private tailrec fun gcd(a: Int, b: Int): Int = if (b == 0) a else gcd(b, a % b)

fun Fraction.reduced(): Fraction {
    val divisor = gcd(kotlin.math.abs(numerator), denominator)
    return Fraction(numerator / divisor, denominator / divisor)
}

operator fun Fraction.plus(other: Fraction): Fraction =
    Fraction(
        numerator * other.denominator + other.numerator * denominator,
        denominator * other.denominator,
    ).reduced()

operator fun Fraction.compareTo(other: Fraction): Int =
    (numerator.toLong() * other.denominator).compareTo(other.numerator.toLong() * denominator)

object Ex20Solution : Ex20 {
    override fun capitalizeWords(text: String): String = text.capitalizeWords()
    override fun add(a: Fraction, b: Fraction): Fraction = a + b
    override fun compare(a: Fraction, b: Fraction): Int = a.compareTo(b)
}
