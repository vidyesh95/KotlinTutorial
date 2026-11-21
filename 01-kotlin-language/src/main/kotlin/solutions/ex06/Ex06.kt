package solutions.ex06

import exercises.ex06.Ex06
import exercises.ex06.User

object Ex06Solution : Ex06 {
    override fun emailDomain(user: User?): String? {
        val email = user?.profile?.email ?: return null
        return if ('@' in email) email.substringAfter('@') else null
    }

    override fun displayName(user: User?): String = user?.profile?.name ?: "Guest"

    override fun sumOfValid(inputs: List<String?>): Int = inputs.mapNotNull { it?.toIntOrNull() }.sum()

    override fun firstLongWord(words: List<String>?, minLength: Int): String? =
        words?.firstOrNull { it.length >= minLength }
}
