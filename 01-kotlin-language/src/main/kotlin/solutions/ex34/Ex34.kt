package solutions.ex34

import exercises.ex34.Ex34
import java.nio.file.Path
import kotlin.io.path.bufferedWriter
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo
import kotlin.io.path.useLines
import kotlin.io.path.walk

private val WHITESPACE = Regex("""\s+""")

object Ex34Solution : Ex34 {
    override fun countWords(file: Path): Int =
        file.useLines { lines -> lines.sumOf { line -> line.split(WHITESPACE).count { it.isNotEmpty() } } }

    override fun writeCsv(file: Path, rows: List<List<String>>) {
        file.bufferedWriter().use { out ->
            rows.forEach { row -> out.write(row.joinToString(",") + "\n") }
        }
    }

    override fun findKotlinFiles(dir: Path): List<String> =
        dir.walk()
            .filter { it.extension == "kt" }
            .map { it.relativeTo(dir).invariantSeparatorsPathString }
            .sorted()
            .toList()
}
