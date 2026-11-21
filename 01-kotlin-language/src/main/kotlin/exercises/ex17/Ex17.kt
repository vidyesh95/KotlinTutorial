package exercises.ex17

/*
 * Exercise 17 — Data, enum and value classes          (lesson: lesson17_data_enum_value_classes)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex17/Ex17.kt.
 */

data class Product(val sku: String, val name: String, val priceCents: Long)

enum class CupSize(val ml: Int) { SMALL(250), MEDIUM(350), LARGE(500) }

/** Money as whole cents. A value class so it can't be confused with any other Long. */
@JvmInline
value class Cents(val value: Long)

interface Ex17 {
    /**
     * 17.1 — Return a NEW product whose price is reduced by [percent] % (round down to whole cents).
     * The original must stay unchanged. Hint: copy().
     * Example: price 1000, 15 % → 850.
     */
    fun applyDiscount(product: Product, percent: Int): Product

    /** 17.2 — Remove products that are exact duplicates (same sku, name AND price), keeping first-seen order. */
    fun uniqueProducts(products: List<Product>): List<Product>

    /** 17.3 — "large", "LARGE", " Large " → CupSize.LARGE. Unknown text → null (don't throw!). */
    fun parseCupSize(text: String): CupSize?

    /** 17.4 — Format money: Cents(1205) → "$12.05", Cents(7) → "$0.07", Cents(100000) → "$1000.00". */
    fun format(money: Cents): String
}

object Ex17Exercise : Ex17 {
    override fun applyDiscount(product: Product, percent: Int): Product = TODO("17.1")
    override fun uniqueProducts(products: List<Product>): List<Product> = TODO("17.2")
    override fun parseCupSize(text: String): CupSize? = TODO("17.3")
    override fun format(money: Cents): String = TODO("17.4")
}
