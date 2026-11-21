package exercises.ex25

/*
 * Exercise 25 — Variance                      (lesson: lesson25_variance)
 * Replace every TODO(...) then run: ./gradlew :01-kotlin-language:exercises
 *
 * Read the tests too: some of them only COMPILE because of `out` and `in` below.
 * Try deleting an `out`/`in` here and watch which test stops compiling — that's the lesson.
 */

/** Gives values out — covariant. */
fun interface Source<out T> {
    fun next(): T
}

/** Takes values in — contravariant. */
fun interface Sink<in T> {
    fun accept(item: T)
}

interface Ex25 {
    /** 25.1 — Call source.next() [count] times and return the values in order. */
    fun <T> take(source: Source<T>, count: Int): List<T>

    /** 25.2 — Feed every item, in order, into [sink]. */
    fun <T> drain(items: List<T>, sink: Sink<T>)

    /** 25.3 — A Source that returns start, start+1, start+2, … on each call. */
    fun countingSource(start: Int): Source<Int>

    /** 25.4 — A Sink that appends everything it accepts to [into]. */
    fun <T> collectingSink(into: MutableList<in T>): Sink<T>
}

object Ex25Exercise : Ex25 {
    override fun <T> take(source: Source<T>, count: Int): List<T> = TODO("25.1")
    override fun <T> drain(items: List<T>, sink: Sink<T>): Unit = TODO("25.2")
    override fun countingSource(start: Int): Source<Int> = TODO("25.3")
    override fun <T> collectingSink(into: MutableList<in T>): Sink<T> = TODO("25.4")
}
