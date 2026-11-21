package solutions.ex21

import exercises.ex21.Ex21
import exercises.ex21.Holder
import exercises.ex21.IntHolder
import exercises.ex21.Tracked
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object Ex21Solution : Ex21 {
    override fun loggingList(log: MutableList<String>): MutableList<String> = LoggingList(log)

    override fun memo(compute: () -> String): Holder = object : Holder {
        override val value: String by lazy(compute)
    }

    override fun clamped(min: Int, max: Int): IntHolder = object : IntHolder {
        override var value: Int by Clamp(min, max)
    }

    override fun tracked(): Tracked = TrackedName()
}

class LoggingList<T>(
    private val log: MutableList<String>,
    private val inner: MutableList<T> = ArrayList(),
) : MutableList<T> by inner {
    override fun add(element: T): Boolean {
        log += "add:$element"
        return inner.add(element)
    }
}

class Clamp(private val min: Int, private val max: Int) : ReadWriteProperty<Any?, Int> {
    private var stored = min
    override fun getValue(thisRef: Any?, property: KProperty<*>): Int = stored
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int) {
        stored = value.coerceIn(min, max)
    }
}

class TrackedName : Tracked {
    override val changes = mutableListOf<String>()
    override var name: String by Delegates.observable("") { _, old, new -> changes += "$old->$new" }
}
