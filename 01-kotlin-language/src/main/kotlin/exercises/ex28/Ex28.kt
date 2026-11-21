package exercises.ex28

/*
 * Exercise 28 — Context parameters          (lesson: lesson28_context_parameters)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex28/Ex28.kt.
 */

/** How money is shown to the user. */
data class Currency(val symbol: String)

interface Ex28 {
    /**
     * 28.1 — Format cents using the [Currency] in context:
     * with Currency("€"): 1234 → "€12.34", 5 → "€0.05", 100 → "€1.00".
     */
    context(currency: Currency)
    fun formatPrice(cents: Long): String

    /**
     * 28.2 — Sum all prices and format the total. Reuse [formatPrice]
     * (notice you don't have to pass the currency to it!).
     * with Currency("$"): [150, 250] → "$4.00"
     */
    context(currency: Currency)
    fun formatTotal(pricesInCents: List<Long>): String

    /** 28.3 — Run [block] with Currency("€") in context and return its result. */
    fun <R> inEuro(block: context(Currency) () -> R): R
}

object Ex28Exercise : Ex28 {
    context(currency: Currency)
    override fun formatPrice(cents: Long): String = TODO("28.1")

    context(currency: Currency)
    override fun formatTotal(pricesInCents: List<Long>): String = TODO("28.2")

    override fun <R> inEuro(block: context(Currency) () -> R): R = TODO("28.3")
}
