package exercises.ex29

/*
 * Exercise 29 — Lambdas with receiver and DSLs          (lesson: lesson29_dsls_and_receivers)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex29/Ex29.kt.
 */

/** Receiver of the CSV-line DSL in 29.1. */
interface CsvLine {
    /** Adds one cell. `null` becomes an empty cell. */
    fun cell(value: Any?)
}

/** Receiver of the menu DSL in 29.3. */
interface MenuBuilder {
    fun item(name: String, priceInCents: Int)
}

interface Ex29 {
    /**
     * 29.1 — Build one CSV line. Cells are joined with ",".
     * csvLine { cell("Dune"); cell(1965); cell(null) } → "Dune,1965,"
     * Hint: write a private class implementing [CsvLine], run `block` on it.
     */
    fun csvLine(block: CsvLine.() -> Unit): String

    /**
     * 29.2 — Run [block] on the receiver only when [condition] is true; always return the receiver.
     * mutableListOf(1).applyIf(true) { add(2) } → [1, 2]
     */
    fun <T> applyIf(receiver: T, condition: Boolean, block: T.() -> Unit): T

    /**
     * 29.3 — Menu DSL. Each item is formatted as "<name>: <euros>.<cents>".
     * menu { item("Tea", 250); item("Cake", 1205) } → ["Tea: 2.50", "Cake: 12.05"]
     */
    fun menu(block: MenuBuilder.() -> Unit): List<String>
}

object Ex29Exercise : Ex29 {
    override fun csvLine(block: CsvLine.() -> Unit): String = TODO("29.1")
    override fun <T> applyIf(receiver: T, condition: Boolean, block: T.() -> Unit): T = TODO("29.2")
    override fun menu(block: MenuBuilder.() -> Unit): List<String> = TODO("29.3")
}
