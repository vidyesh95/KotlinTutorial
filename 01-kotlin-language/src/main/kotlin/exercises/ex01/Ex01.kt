package exercises.ex01

/*
 * Exercise 01 — Hello, World          (lesson: lesson01_hello_world)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex01*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex01/Ex01.kt.
 */

interface Ex01 {
    /** 01.1 — Return "Hello, <name>!" — e.g. greeting("Ada") == "Hello, Ada!". */
    fun greeting(name: String): String

    /**
     * 01.2 — Draw a banner around [text] with '*', one space of padding, and "\n" between lines.
     * banner("Hi") must return these three lines:
     * ```
     * ******
     * * Hi *
     * ******
     * ```
     * Hint: "*".repeat(n) repeats a string n times; text.length is its length.
     */
    fun banner(text: String): String
}

object Ex01Exercise : Ex01 {
    override fun greeting(name: String): String = TODO("01.1")
    override fun banner(text: String): String = TODO("01.2")
}
