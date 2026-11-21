package solutions.ex04

import exercises.ex04.Ex04

object Ex04Solution : Ex04 {
    override fun initials(fullName: String): String =
        fullName.split(" ")
            .filter { it.isNotBlank() }
            .joinToString("") { it.first().uppercase() }

    override fun isPalindrome(text: String): Boolean {
        val clean = text.lowercase().filter { it.isLetterOrDigit() }
        return clean == clean.reversed()
    }

    override fun orderNumber(n: Int): String = "ORD-" + n.toString().padStart(5, '0')

    override fun formatPrice(cents: Long): String {
        val dollars = cents / 100
        val rest = (cents % 100).toString().padStart(2, '0')
        return "\$$dollars.$rest"     // \$ is a literal dollar sign
    }
}
