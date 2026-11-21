package exercises.ex34

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.io.TempDir
import solutions.ex34.Ex34Solution
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.readText
import kotlin.io.path.writeText
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex34Spec(private val impl: Ex34) {

    @TempDir
    lateinit var dir: Path                  // JUnit creates a fresh temp folder per test and deletes it after

    @Test
    fun `34_1 countWords handles any whitespace`() {
        val file = dir / "text.txt"
        file.writeText("one two  three\n\tfour\n\nfive   \n")
        assertEquals(5, impl.countWords(file))
    }

    @Test
    fun `34_2 writeCsv writes one line per row`() {
        val file = dir / "out.csv"
        impl.writeCsv(file, listOf(listOf("id", "title"), listOf("1", "Dune")))
        assertEquals("id,title\n1,Dune\n", file.readText())
    }

    @Test
    fun `34_3 findKotlinFiles walks recursively`() {
        (dir / "model").createDirectories()
        (dir / "Main.kt").writeText("")
        (dir / "notes.md").writeText("")
        (dir / "model" / "Book.kt").writeText("")
        assertEquals(listOf("Main.kt", "model/Book.kt"), impl.findKotlinFiles(dir))
    }
}

class Ex34SolutionTest : Ex34Spec(Ex34Solution)

@Tag("exercise")
class Ex34ExerciseTest : Ex34Spec(Ex34Exercise)
