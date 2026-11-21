package aoc

/**
 * Folder layout for each day:
 * ```
 * src/main/resources/advent_of_code/year2021/day1/
 *   Day1.txt    <- puzzle description (committed)
 *   input.txt   <- YOUR personal puzzle input from adventofcode.com (git-ignored, don't share it)
 * ```
 */
private fun resource(year: Int, day: Int, file: String): List<String> {
    val path = "/advent_of_code/year$year/day$day/$file"
    val stream = object {}.javaClass.getResourceAsStream(path)
        ?: error("Not found on classpath: src/main/resources$path")
    return stream.bufferedReader().readLines()
}

/** Your personal puzzle input. */
fun readInput(year: Int, day: Int): List<String> = resource(year, day, "input.txt")

/** The puzzle text, handy to read while solving. */
fun readDescription(year: Int, day: Int): List<String> = resource(year, day, "Day$day.txt")

fun main() {
    val description = readDescription(year = 2021, day = 1)
    println(description.first())
    println(description.last().trim())
}
