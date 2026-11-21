package exercises.ex04

/*
 * Exercise 04 — Strings and chars          (lesson: lesson04_strings_and_chars)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises --tests 'exercises.ex04*'
 * Stuck? Re-read the lesson first. The answer is in solutions/ex04/Ex04.kt.
 */

interface Ex04 {
    /** 04.1 — Upper-case initials: initials("ada  lovelace") == "AL", initials("grace") == "G". Ignore extra spaces. */
    fun initials(fullName: String): String

    /**
     * 04.2 — Palindrome check that ignores case and everything that isn't a letter or digit.
     * isPalindrome("Race car!") == true, isPalindrome("Kotlin") == false.
     */
    fun isPalindrome(text: String): Boolean

    /** 04.3 — Order numbers are "ORD-" + 5 digits with leading zeros: orderNumber(42) == "ORD-00042". */
    fun orderNumber(n: Int): String

    /**
     * 04.4 — Format cents as dollars: formatPrice(1999) == "$19.99", formatPrice(5) == "$0.05",
     * formatPrice(300) == "$3.00". Hint: a `$` needs escaping in normal strings (or use $$"...").
     */
    fun formatPrice(cents: Long): String
}

object Ex04Exercise : Ex04 {
    override fun initials(fullName: String): String = TODO("04.1")
    override fun isPalindrome(text: String): Boolean = TODO("04.2")
    override fun orderNumber(n: Int): String = TODO("04.3")
    override fun formatPrice(cents: Long): String = TODO("04.4")
}
