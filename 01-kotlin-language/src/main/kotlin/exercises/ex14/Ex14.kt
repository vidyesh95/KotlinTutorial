package exercises.ex14

/*
 * Exercise 14 — scope functions          (lesson: lesson14_scope_functions)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex14/Ex14.kt.
 */

data class HttpRequest(
    var url: String = "",
    var method: String = "GET",
    val headers: MutableMap<String, String> = mutableMapOf(),
)

interface Ex14 {
    /** 14.1 — A POST request to [url] with header "Accept" = "application/json". Use `apply`. */
    fun jsonPost(url: String): HttpRequest

    /** 14.2 — The part after '@', or "unknown" if [email] is null or has no '@'. Try `?.let` / `takeIf` + `?:`. */
    fun domainOf(email: String?): String

    /** 14.3 — [text] as Int only if it is a number greater than 0, else null. Use `takeIf`. */
    fun parsePositive(text: String): Int?

    /**
     * 14.4 — Trim + lowercase every name, drop blank ones, then (using `also`) append
     * "normalized N names" to [log] where N is the result size. Return the normalized list.
     */
    fun normalize(names: List<String>, log: MutableList<String>): List<String>
}

object Ex14Exercise : Ex14 {
    override fun jsonPost(url: String): HttpRequest = TODO("14.1")
    override fun domainOf(email: String?): String = TODO("14.2")
    override fun parsePositive(text: String): Int? = TODO("14.3")
    override fun normalize(names: List<String>, log: MutableList<String>): List<String> = TODO("14.4")
}
