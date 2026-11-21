package exercises.ex23

/*
 * Exercise 23 — Generics, part 1          (lesson: lesson23_generics_intro)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex23/Ex23.kt.
 */

/** A last-in-first-out stack, like a pile of plates. */
interface Stack<T> {
    val size: Int
    fun push(item: T)

    /** Removes and returns the top item, or null when empty. */
    fun pop(): T?

    /** Returns the top item without removing it, or null when empty. */
    fun peek(): T?
}

interface Ex23 {
    /** 23.1 — Every element twice, in order: [1, 2] → [1, 1, 2, 2]. */
    fun <T> doubleEach(items: List<T>): List<T>

    /** 23.2 — Swap the two halves of a pair: (1, "a") → ("a", 1). */
    fun <A, B> swap(pair: Pair<A, B>): Pair<B, A>

    /** 23.3 — Return a new, empty [Stack]. You'll need to write a class `ListStack<T> : Stack<T>`. */
    fun <T> newStack(): Stack<T>
}

object Ex23Exercise : Ex23 {
    override fun <T> doubleEach(items: List<T>): List<T> = TODO("23.1")
    override fun <A, B> swap(pair: Pair<A, B>): Pair<B, A> = TODO("23.2")
    override fun <T> newStack(): Stack<T> = TODO("23.3")
}
