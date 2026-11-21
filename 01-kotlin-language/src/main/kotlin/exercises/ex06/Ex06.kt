package exercises.ex06

/*
 * Exercise 06 — Null safety          (lesson: lesson06_null_safety)
 *
 * Rule for this exercise: do NOT use `!!`. Use ?. ?: let and smart casts.
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex06*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex06/Ex06.kt.
 */

data class Profile(val name: String, val email: String?)
data class User(val id: Int, val profile: Profile?)

interface Ex06 {
    /**
     * 06.1 — The domain of the user's email: "ada@example.com" → "example.com".
     * Return null if the user, the profile or the email is missing, or the email has no '@'.
     */
    fun emailDomain(user: User?): String?

    /** 06.2 — The profile name, or "Guest" if the user or profile is missing. */
    fun displayName(user: User?): String

    /**
     * 06.3 — Sum all entries that are valid integers; skip nulls and garbage.
     * sumOfValid(listOf("1", null, "x", "41")) == 42
     */
    fun sumOfValid(inputs: List<String?>): Int

    /**
     * 06.4 — The first word with at least [minLength] characters, or null.
     * The LIST itself may be null too: firstLongWord(null, 3) == null.
     */
    fun firstLongWord(words: List<String>?, minLength: Int): String?
}

object Ex06Exercise : Ex06 {
    override fun emailDomain(user: User?): String? = TODO("06.1")
    override fun displayName(user: User?): String = TODO("06.2")
    override fun sumOfValid(inputs: List<String?>): Int = TODO("06.3")
    override fun firstLongWord(words: List<String>?, minLength: Int): String? = TODO("06.4")
}
