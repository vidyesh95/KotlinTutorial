package solutions.ex14

import exercises.ex14.Ex14
import exercises.ex14.HttpRequest

object Ex14Solution : Ex14 {
    override fun jsonPost(url: String): HttpRequest = HttpRequest().apply {
        this.url = url                       // `this.` needed: the parameter has the same name
        method = "POST"
        headers["Accept"] = "application/json"
    }

    override fun domainOf(email: String?): String =
        email?.takeIf { '@' in it }?.let { it.substringAfter('@') } ?: "unknown"

    override fun parsePositive(text: String): Int? = text.toIntOrNull()?.takeIf { it > 0 }

    override fun normalize(names: List<String>, log: MutableList<String>): List<String> =
        names.map { it.trim().lowercase() }
            .filter { it.isNotEmpty() }
            .also { log += "normalized ${it.size} names" }
}
