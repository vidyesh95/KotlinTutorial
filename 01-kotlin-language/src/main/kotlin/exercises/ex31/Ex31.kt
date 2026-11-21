package exercises.ex31

import kotlinx.coroutines.flow.Flow

/*
 * Exercise 31 — Cancellation, timeouts and Flow          (lesson: lesson31_coroutines_flow_and_cancellation)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex31/Ex31.kt.
 */
interface Ex31 {
    /** 31.1 — A flow that emits [from], from-1, …, 0 (use the `flow { }` builder). countdown(2) → 2, 1, 0 */
    fun countdown(from: Int): Flow<Int>

    /** 31.2 — The first [n] squares of the EVEN numbers in [numbers]. [1,2,3,4,5,6,8], n=2 → [4, 16] */
    suspend fun firstEvenSquares(numbers: Flow<Int>, n: Int): List<Int>

    /** 31.3 — Run [block]; if it takes longer than [timeoutMillis], give up and return null. */
    suspend fun <T> orNullAfter(timeoutMillis: Long, block: suspend () -> T): T?
}

object Ex31Exercise : Ex31 {
    override fun countdown(from: Int): Flow<Int> = TODO("31.1")
    override suspend fun firstEvenSquares(numbers: Flow<Int>, n: Int): List<Int> = TODO("31.2")
    override suspend fun <T> orNullAfter(timeoutMillis: Long, block: suspend () -> T): T? = TODO("31.3")
}
