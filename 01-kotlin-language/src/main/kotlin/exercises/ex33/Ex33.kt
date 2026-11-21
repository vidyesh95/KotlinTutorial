package exercises.ex33

/*
 * Exercise 33 — Regular expressions          (lesson: lesson33_regex)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex33/Ex33.kt.
 */

data class LogEntry(val time: String, val level: String, val module: String, val message: String)

interface Ex33 {
    /**
     * 33.1 — A username is valid when it is 3–16 chars of lowercase letters, digits or `_`,
     * and starts with a letter. "ada_99" ✅  "9lives" ❌  "ab" ❌  "Ada" ❌
     */
    fun isValidUsername(name: String): Boolean

    /** 33.2 — All hashtags in order, without the `#`. "I #love #kotlin2!" → ["love", "kotlin2"] */
    fun hashtags(text: String): List<String>

    /**
     * 33.3 — Parse "HH:MM:SS LEVEL [module] message" (LEVEL is INFO, WARN or ERROR).
     * Return null when the line doesn't fit.
     * "12:00:01 WARN [db] Slow query" → LogEntry("12:00:01", "WARN", "db", "Slow query")
     */
    fun parseLogLine(line: String): LogEntry?
}

object Ex33Exercise : Ex33 {
    override fun isValidUsername(name: String): Boolean = TODO("33.1")
    override fun hashtags(text: String): List<String> = TODO("33.2")
    override fun parseLogLine(line: String): LogEntry? = TODO("33.3")
}
