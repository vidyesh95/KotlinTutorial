package solutions.ex01

import exercises.ex01.Ex01

object Ex01Solution : Ex01 {
    override fun greeting(name: String): String = "Hello, $name!"

    override fun banner(text: String): String {
        val border = "*".repeat(text.length + 4)
        return "$border\n* $text *\n$border"
    }
}
