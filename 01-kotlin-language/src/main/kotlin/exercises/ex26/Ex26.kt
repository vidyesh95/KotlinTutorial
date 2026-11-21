package exercises.ex26

/*
 * Exercise 26 — Use-site variance and star projections   (lesson: lesson26_use_site_variance_and_star)
 * Replace every TODO(...) then run: ./gradlew :01-kotlin-language:exercises
 */
interface Ex26 {
    /** 26.1 — Append all of [from] to the end of [to]. */
    fun <T> copyAll(from: List<T>, to: MutableList<in T>)

    /** 26.2 — Move every element of [from] into [to] (append), then empty [from]. */
    fun <T> moveAll(from: MutableList<out T>, to: MutableList<in T>)

    /**
     * 26.3 — Describe any value:
     *  null → "null", a List → "list(<size>)", a Set → "set(<size>)", a Map → "map(<size>)", anything else → "other".
     */
    fun kind(value: Any?): String

    /** 26.4 — Count the non-null elements of a collection of unknown type. */
    fun countNonNull(items: Collection<*>): Int
}

object Ex26Exercise : Ex26 {
    override fun <T> copyAll(from: List<T>, to: MutableList<in T>): Unit = TODO("26.1")
    override fun <T> moveAll(from: MutableList<out T>, to: MutableList<in T>): Unit = TODO("26.2")
    override fun kind(value: Any?): String = TODO("26.3")
    override fun countNonNull(items: Collection<*>): Int = TODO("26.4")
}
