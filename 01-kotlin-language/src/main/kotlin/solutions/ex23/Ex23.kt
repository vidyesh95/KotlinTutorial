package solutions.ex23

import exercises.ex23.Ex23
import exercises.ex23.Stack

object Ex23Solution : Ex23 {
    override fun <T> doubleEach(items: List<T>): List<T> = items.flatMap { listOf(it, it) }

    override fun <A, B> swap(pair: Pair<A, B>): Pair<B, A> = pair.second to pair.first

    override fun <T> newStack(): Stack<T> = ListStack()
}

class ListStack<T> : Stack<T> {
    private val items = ArrayDeque<T>()

    override val size: Int get() = items.size
    override fun push(item: T) = items.addLast(item)
    override fun pop(): T? = items.removeLastOrNull()
    override fun peek(): T? = items.lastOrNull()
}
