package solutions.ex30

import exercises.ex30.Ex30
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

object Ex30Solution : Ex30 {
    override suspend fun fetchAll(ids: List<Int>, fetch: suspend (Int) -> String): List<String> =
        coroutineScope { ids.map { id -> async { fetch(id) } }.awaitAll() }

    override suspend fun sumInBackground(numbers: List<Int>): Long =
        withContext(Dispatchers.Default) { numbers.sumOf { it.toLong() } }

    override suspend fun <T> retry(times: Int, delayMillis: Long, block: suspend () -> T): T {
        repeat(times - 1) {
            try {
                return block()
            } catch (e: CancellationException) {
                throw e                      // never swallow cancellation (lesson 31)
            } catch (_: Exception) {
                delay(delayMillis)
            }
        }
        return block()                       // last attempt: exception escapes
    }
}
