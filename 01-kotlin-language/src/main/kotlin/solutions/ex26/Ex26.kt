package solutions.ex26

import exercises.ex26.Ex26

object Ex26Solution : Ex26 {
    override fun <T> copyAll(from: List<T>, to: MutableList<in T>) {
        to.addAll(from)
    }

    override fun <T> moveAll(from: MutableList<out T>, to: MutableList<in T>) {
        to.addAll(from)
        from.clear()   // clear() takes no T, so it's allowed on an `out`-projected list
    }

    override fun kind(value: Any?): String = when (value) {
        null -> "null"
        is List<*> -> "list(${value.size})"
        is Set<*> -> "set(${value.size})"
        is Map<*, *> -> "map(${value.size})"
        else -> "other"
    }

    override fun countNonNull(items: Collection<*>): Int = items.count { it != null }
}
