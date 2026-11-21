package solutions.ex25

import exercises.ex25.Ex25
import exercises.ex25.Sink
import exercises.ex25.Source

object Ex25Solution : Ex25 {
    override fun <T> take(source: Source<T>, count: Int): List<T> = List(count) { source.next() }

    override fun <T> drain(items: List<T>, sink: Sink<T>) {
        items.forEach(sink::accept)
    }

    override fun countingSource(start: Int): Source<Int> {
        var current = start
        return Source { current++ }
    }

    override fun <T> collectingSink(into: MutableList<in T>): Sink<T> = Sink { into.add(it) }
}
