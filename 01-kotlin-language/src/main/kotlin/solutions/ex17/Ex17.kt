package solutions.ex17

import exercises.ex17.Cents
import exercises.ex17.CupSize
import exercises.ex17.Ex17
import exercises.ex17.Product

object Ex17Solution : Ex17 {
    override fun applyDiscount(product: Product, percent: Int): Product =
        product.copy(priceCents = product.priceCents * (100 - percent) / 100)

    // distinct() relies on the equals/hashCode that `data class` generated.
    override fun uniqueProducts(products: List<Product>): List<Product> = products.distinct()

    override fun parseCupSize(text: String): CupSize? =
        CupSize.entries.firstOrNull { it.name.equals(text.trim(), ignoreCase = true) }

    override fun format(money: Cents): String {
        val dollars = money.value / 100
        val cents = money.value % 100
        return "$" + dollars + "." + cents.toString().padStart(2, '0')
    }
}
