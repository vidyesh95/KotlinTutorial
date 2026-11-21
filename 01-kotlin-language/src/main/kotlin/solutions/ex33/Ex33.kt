package solutions.ex33

import exercises.ex33.Ex33
import exercises.ex33.LogEntry

private val USERNAME = Regex("""^[a-z][a-z0-9_]{2,15}$""")
private val HASHTAG = Regex("""#(\w+)""")
private val LOG_LINE = Regex("""^(\d{2}:\d{2}:\d{2}) (INFO|WARN|ERROR) \[(\w+)] (.+)$""")

object Ex33Solution : Ex33 {
    override fun isValidUsername(name: String): Boolean = USERNAME.matches(name)

    override fun hashtags(text: String): List<String> =
        HASHTAG.findAll(text).map { it.groupValues[1] }.toList()

    override fun parseLogLine(line: String): LogEntry? {
        val (time, level, module, message) = LOG_LINE.matchEntire(line)?.destructured ?: return null
        return LogEntry(time, level, module, message)
    }
}
