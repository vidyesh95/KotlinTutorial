package exercises.ex07

/*
 * Exercise 07 — Collections          (lesson: lesson07_collections)
 *
 * Challenge: solve each task WITHOUT writing a loop — chain collection functions.
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex07*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex07/Ex07.kt.
 */

data class Purchase(val customer: String, val amount: Int)

interface Ex07 {
    /**
     * 07.1 — Count each word, case-insensitively, words separated by spaces (ignore extra spaces).
     * wordFrequencies("The cat  the") == mapOf("the" to 2, "cat" to 1)
     * Hint: groupBy { it }.mapValues { ... } — or look up groupingBy { it }.eachCount().
     */
    fun wordFrequencies(text: String): Map<String, Int>

    /**
     * 07.2 — Names of the [n] customers who spent the most in total, highest first.
     * On equal totals, alphabetical order by name.
     */
    fun topSpenders(purchases: List<Purchase>, n: Int): List<String>

    /** 07.3 — Differences between neighbours: [1, 4, 9, 16] → [3, 5, 7]. Fewer than 2 values → empty. */
    fun neighbourDifferences(values: List<Int>): List<Int>

    /**
     * 07.4 — The smallest perfect square (1, 4, 9, 16, …) that is greater than [limit].
     * firstSquareAbove(50) == 64. Hint: generateSequence(1) { it + 1 }.map { ... }.first { ... }
     */
    fun firstSquareAbove(limit: Int): Int
}

object Ex07Exercise : Ex07 {
    override fun wordFrequencies(text: String): Map<String, Int> = TODO("07.1")
    override fun topSpenders(purchases: List<Purchase>, n: Int): List<String> = TODO("07.2")
    override fun neighbourDifferences(values: List<Int>): List<Int> = TODO("07.3")
    override fun firstSquareAbove(limit: Int): Int = TODO("07.4")
}
