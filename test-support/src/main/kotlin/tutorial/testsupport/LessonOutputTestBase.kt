package tutorial.testsupport

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.reflect.InvocationTargetException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.extension
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.readLines
import kotlin.io.path.walk
import kotlin.test.assertEquals

/**
 * Keeps the lessons honest: runs every lesson's `main()` and checks that it prints
 * exactly what the lesson's `// Output:` block claims.
 *
 * Markers understood at the bottom of a lesson file:
 * ```
 * // Input:            <- optional, lines fed to readln()
 * // 42
 * //
 * // Output:           <- exact expected stdout
 * // ...
 * // Output (varies):  <- output changes between runs (time, threads, random): only checks main() doesn't crash
 * ```
 * Run only some lessons: `./gradlew :01-kotlin-language:test -PlessonFilter=lesson23`
 *
 * Each module enables it with one line in its tests:
 * ```
 * class LessonOutputTest : LessonOutputTestBase()
 * ```
 */
abstract class LessonOutputTestBase {

    @TestFactory
    fun `every lesson prints exactly its Output block`(): List<DynamicTest> {
        val filter = System.getProperty("lessonFilter").orEmpty()
        return Path("src/main/kotlin").walk()
            .filter { it.extension == "kt" && "/lesson" in it.invariantSeparatorsPathString }
            .mapNotNull(LessonFile::parse)
            .filter { filter.isEmpty() || filter in it.className }
            .sortedBy { it.className }
            .map { lesson -> DynamicTest.dynamicTest(lesson.className) { lesson.verify() } }
            .toList()
    }
}

private data class LessonFile(
    val className: String,
    val input: List<String>,
    val expectedOutput: List<String>?, // null = "varies"
) {
    fun verify() {
        val actual = runMain().lines().map(String::trimEnd).joinToString("\n").trimEnd()
        if (expectedOutput != null) {
            val expected = expectedOutput.map(String::trimEnd).joinToString("\n").trimEnd()
            assertEquals(expected, actual, "Output of $className does not match its // Output: block")
        }
    }

    private fun runMain(): String {
        val captured = ByteArrayOutputStream()
        val originalOut = System.out
        val originalIn = System.`in`
        System.setOut(PrintStream(captured, true, Charsets.UTF_8))
        System.setIn(ByteArrayInputStream(input.joinToString("\n", postfix = "\n").toByteArray()))
        try {
            Class.forName(className).getMethod("main", Array<String>::class.java).invoke(null, emptyArray<String>())
        } catch (e: InvocationTargetException) {
            throw AssertionError("main() of $className threw ${e.targetException}", e.targetException)
        } finally {
            System.setOut(originalOut)
            System.setIn(originalIn)
        }
        return captured.toString(Charsets.UTF_8).replace("\r\n", "\n")
    }

    companion object {
        fun parse(file: Path): LessonFile? {
            val lines = file.readLines()
            if (lines.none { it.startsWith("fun main(") }) return null
            val pkg = lines.firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim() ?: return null
            val className = "$pkg.${file.nameWithoutExtension}Kt"
            val input = block(lines, "// Input:").orEmpty()
            return when {
                lines.any { it.trim() == "// Output (varies):" } -> LessonFile(className, input, null)
                else -> LessonFile(className, input, block(lines, "// Output:") ?: return null)
            }
        }

        /** Consecutive `//` lines after [header], with the `// ` prefix removed. */
        private fun block(lines: List<String>, header: String): List<String>? {
            val start = lines.indexOfFirst { it.trim() == header }
            if (start == -1) return null
            return lines.drop(start + 1)
                .takeWhile { it.startsWith("//") && it.trim() != "// Output:" && it.trim() != "// Output (varies):" }
                .map { it.removePrefix("//").removePrefix(" ") }
        }
    }
}
