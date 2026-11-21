package solutions.ex13

import exercises.ex13.Ex13

object Ex13Solution : Ex13 {
    override fun <T> myFilter(items: List<T>, keep: (T) -> Boolean): List<T> {
        val result = mutableListOf<T>()
        for (item in items) if (keep(item)) result += item
        return result
    }

    override fun <T, R> myMap(items: List<T>, transform: (T) -> R): List<R> {
        val result = mutableListOf<R>()
        for (item in items) result += transform(item)
        return result
    }

    override fun andThen(first: (Int) -> Int, second: (Int) -> Int): (Int) -> Int = { second(first(it)) }

    override fun firstNegativeIndex(numbers: List<Int>): Int {
        numbers.forEachIndexed { index, n ->
            if (n < 0) return index          // non-local return: forEachIndexed is inline
        }
        return -1
    }
}
