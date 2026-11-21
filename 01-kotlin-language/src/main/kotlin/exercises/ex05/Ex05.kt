package exercises.ex05

/*
 * Exercise 05 — Input and output          (lesson: lesson05_input_output)
 *
 * Reading from the keyboard is hard to test, so these exercises practise the
 * PARSING part: turning a line of text (as readln() would give you) into data.
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex05*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex05/Ex05.kt.
 */

interface Ex05 {
    /**
     * 05.1 — Numbers separated by one OR MORE spaces, maybe with spaces around:
     * parseNumbers("3 4 5") == listOf(3, 4, 5), parseNumbers("  1   2 ") == listOf(1, 2),
     * parseNumbers("") == emptyList().
     */
    fun parseNumbers(line: String): List<Int>

    /**
     * 05.2 — A human age: a whole number from 0 to 150 (surrounding spaces allowed).
     * Anything else → null. parseAge(" 36 ") == 36, parseAge("abc") == null, parseAge("-1") == null.
     */
    fun parseAge(text: String): Int?

    /**
     * 05.3 — A config line "key=value" → Pair(key, value), both trimmed.
     * parseKeyValue(" port = 8080 ") == ("port" to "8080"). Only the FIRST '=' splits:
     * parseKeyValue("url=a=b") == ("url" to "a=b"). No '=' or empty key → null.
     * Hint: indexOf, substringBefore, substringAfter.
     */
    fun parseKeyValue(line: String): Pair<String, String>?
}

object Ex05Exercise : Ex05 {
    override fun parseNumbers(line: String): List<Int> = TODO("05.1")
    override fun parseAge(text: String): Int? = TODO("05.2")
    override fun parseKeyValue(line: String): Pair<String, String>? = TODO("05.3")
}
