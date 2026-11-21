package solutions.ex09

import exercises.ex09.Ex09

object Ex09Solution : Ex09 {
    override fun sumOfMultiples(limit: Int): Int {
        var sum = 0
        for (n in 1..<limit) {
            if (n % 3 == 0 || n % 5 == 0) sum += n
        }
        return sum
    }

    override fun evensDown(from: Int): List<Int> {
        val start = if (from % 2 == 0) from else from - 1
        return (start downTo 0 step 2).toList()
    }

    override fun firstDuplicate(items: List<String>): String? {
        val seen = mutableSetOf<String>()
        for (item in items) {
            if (!seen.add(item)) return item        // add() returns false if already present
        }
        return null
    }

    override fun findPair(numbers: List<Int>, target: Int): Pair<Int, Int>? {
        var found: Pair<Int, Int>? = null
        outer@ for (i in numbers.indices) {
            for (j in i + 1..<numbers.size) {
                if (numbers[i] + numbers[j] == target) {
                    found = i to j
                    break@outer
                }
            }
        }
        return found
    }
}
