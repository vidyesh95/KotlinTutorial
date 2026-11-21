package solutions.ex31

import exercises.ex31.Ex31
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeoutOrNull

object Ex31Solution : Ex31 {
    override fun countdown(from: Int): Flow<Int> = flow {
        for (i in from downTo 0) emit(i)
    }

    override suspend fun firstEvenSquares(numbers: Flow<Int>, n: Int): List<Int> =
        numbers.filter { it % 2 == 0 }.map { it * it }.take(n).toList()

    override suspend fun <T> orNullAfter(timeoutMillis: Long, block: suspend () -> T): T? =
        withTimeoutOrNull(timeoutMillis) { block() }
}
