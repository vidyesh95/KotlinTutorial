package solutions.ex05

import exercises.ex05.Ex05

object Ex05Solution : Ex05 {
    override fun parseNumbers(line: String): List<Int> =
        line.split(" ")
            .filter { it.isNotBlank() }
            .map { it.toInt() }

    override fun parseAge(text: String): Int? {
        val age = text.trim().toIntOrNull() ?: return null
        return if (age in 0..150) age else null
    }

    override fun parseKeyValue(line: String): Pair<String, String>? {
        if ('=' !in line) return null
        val key = line.substringBefore('=').trim()
        val value = line.substringAfter('=').trim()
        return if (key.isEmpty()) null else key to value
    }
}
