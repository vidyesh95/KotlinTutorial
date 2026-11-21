package exercises.ex21

/*
 * Exercise 21 — Delegation          (lesson: lesson21_delegation)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex21/Ex21.kt.
 */

interface Holder {
    val value: String
}

interface IntHolder {
    var value: Int
}

interface Tracked {
    var name: String

    /** Every change of [name] as "old->new", oldest first. */
    val changes: List<String>
}

interface Ex21 {
    /**
     * 21.1 — Return a MutableList that behaves like a normal ArrayList, but every call to
     * add(element) also appends "add:<element>" to [log].
     * Hint: `class LoggingList<T>(…) : MutableList<T> by inner` and override add(element).
     */
    fun loggingList(log: MutableList<String>): MutableList<String>

    /**
     * 21.2 — Return a Holder whose value comes from [compute], called ONLY on the first read
     * (never if nobody reads it) and never again afterwards. Hint: `by lazy`.
     */
    fun memo(compute: () -> String): Holder

    /**
     * 21.3 — Return an IntHolder starting at [min]. Writes are CLAMPED into min..max:
     * writing max+10 stores max, writing min-5 stores min. Hint: a ReadWriteProperty or a custom setter.
     */
    fun clamped(min: Int, max: Int): IntHolder

    /** 21.4 — Return a Tracked whose name starts as "" and records each change. Hint: Delegates.observable. */
    fun tracked(): Tracked
}

object Ex21Exercise : Ex21 {
    override fun loggingList(log: MutableList<String>): MutableList<String> = TODO("21.1")
    override fun memo(compute: () -> String): Holder = TODO("21.2")
    override fun clamped(min: Int, max: Int): IntHolder = TODO("21.3")
    override fun tracked(): Tracked = TODO("21.4")
}
