package solutions.ex07

import exercises.ex07.Ex07
import exercises.ex07.Purchase

object Ex07Solution : Ex07 {
    override fun wordFrequencies(text: String): Map<String, Int> =
        text.lowercase()
            .split(" ")
            .filter { it.isNotBlank() }
            .groupingBy { it }
            .eachCount()

    override fun topSpenders(purchases: List<Purchase>, n: Int): List<String> =
        purchases.groupBy { it.customer }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(n)
            .map { it.key }

    override fun neighbourDifferences(values: List<Int>): List<Int> = values.zipWithNext { a, b -> b - a }

    override fun firstSquareAbove(limit: Int): Int =
        generateSequence(1) { it + 1 }
            .map { it * it }
            .first { it > limit }
}
