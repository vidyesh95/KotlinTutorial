package exercises.ex30

/*
 * Exercise 30 — Coroutines basics          (lesson: lesson30_coroutines_basics)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex30/Ex30.kt.
 */
interface Ex30 {
    /**
     * 30.1 — Call [fetch] for every id CONCURRENTLY (all at the same time, using `async`)
     * and return the results in the same order as [ids].
     * Hint: `coroutineScope { ids.map { async { ... } }.awaitAll() }`
     */
    suspend fun fetchAll(ids: List<Int>, fetch: suspend (Int) -> String): List<String>

    /** 30.2 — Sum the numbers on Dispatchers.Default (use `withContext`). */
    suspend fun sumInBackground(numbers: List<Int>): Long

    /**
     * 30.3 — Call [block]; if it throws, wait [delayMillis] (with `delay`) and try again.
     * Try at most [times] times in total; if the last try fails, let its exception escape.
     */
    suspend fun <T> retry(times: Int, delayMillis: Long, block: suspend () -> T): T
}

object Ex30Exercise : Ex30 {
    override suspend fun fetchAll(ids: List<Int>, fetch: suspend (Int) -> String): List<String> = TODO("30.1")
    override suspend fun sumInBackground(numbers: List<Int>): Long = TODO("30.2")
    override suspend fun <T> retry(times: Int, delayMillis: Long, block: suspend () -> T): T = TODO("30.3")
}
