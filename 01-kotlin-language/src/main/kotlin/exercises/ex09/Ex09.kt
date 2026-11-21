package exercises.ex09

/*
 * Exercise 09 — loops and ranges          (lesson: lesson09_loops_and_ranges)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex09/Ex09.kt.
 */

interface Ex09 {
    /** 09.1 — Sum of all numbers BELOW [limit] divisible by 3 or 5. sumOfMultiples(10) == 23 (3+5+6+9). */
    fun sumOfMultiples(limit: Int): Int

    /** 09.2 — Even numbers from [from] down to 0, using `downTo` and `step`. 9 → [8, 6, 4, 2, 0]. */
    fun evensDown(from: Int): List<Int>

    /** 09.3 — The first item that appears a second time, or null. ["a","b","a","b"] → "a". */
    fun firstDuplicate(items: List<String>): String?

    /**
     * 09.4 — Indexes (i, j), i < j, of the FIRST pair (smallest i, then smallest j) whose values sum to [target],
     * or null. Use two nested loops and a labelled `break`/return.  [3, 1, 4, 2], target 5 → (0, 3).
     */
    fun findPair(numbers: List<Int>, target: Int): Pair<Int, Int>?
}

object Ex09Exercise : Ex09 {
    override fun sumOfMultiples(limit: Int): Int = TODO("09.1")
    override fun evensDown(from: Int): List<Int> = TODO("09.2")
    override fun firstDuplicate(items: List<String>): String? = TODO("09.3")
    override fun findPair(numbers: List<Int>, target: Int): Pair<Int, Int>? = TODO("09.4")
}
