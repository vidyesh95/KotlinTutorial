package solutions.ex28

import exercises.ex28.Currency
import exercises.ex28.Ex28

object Ex28Solution : Ex28 {
    context(currency: Currency)
    override fun formatPrice(cents: Long): String =
        "${currency.symbol}${cents / 100}.${(cents % 100).toString().padStart(2, '0')}"

    context(currency: Currency)
    override fun formatTotal(pricesInCents: List<Long>): String = formatPrice(pricesInCents.sum())

    override fun <R> inEuro(block: context(Currency) () -> R): R = context(Currency("€")) { block() }
}
