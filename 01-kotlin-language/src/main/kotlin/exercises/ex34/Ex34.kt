package exercises.ex34

import java.nio.file.Path

/*
 * Exercise 34 — Files and I/O          (lesson: lesson34_files_and_io)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex34/Ex34.kt.
 */
interface Ex34 {
    /**
     * 34.1 — Count the words in [file]. Words are separated by any whitespace (spaces, tabs, newlines).
     * Stream the file with `useLines` instead of loading it at once.
     */
    fun countWords(file: Path): Int

    /** 34.2 — Write [rows] as CSV: cells joined by ",", one row per line, every line ends with "\n". */
    fun writeCsv(file: Path, rows: List<List<String>>)

    /**
     * 34.3 — All `.kt` files anywhere under [dir], as paths RELATIVE to [dir] using "/" separators,
     * sorted alphabetically. e.g. ["Main.kt", "model/Book.kt"]
     */
    fun findKotlinFiles(dir: Path): List<String>
}

object Ex34Exercise : Ex34 {
    override fun countWords(file: Path): Int = TODO("34.1")
    override fun writeCsv(file: Path, rows: List<List<String>>): Unit = TODO("34.2")
    override fun findKotlinFiles(dir: Path): List<String> = TODO("34.3")
}
