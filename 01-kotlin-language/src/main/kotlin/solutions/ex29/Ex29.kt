package solutions.ex29

import exercises.ex29.CsvLine
import exercises.ex29.Ex29
import exercises.ex29.MenuBuilder

object Ex29Solution : Ex29 {
    override fun csvLine(block: CsvLine.() -> Unit): String = CsvLineBuilder().apply(block).build()

    override fun <T> applyIf(receiver: T, condition: Boolean, block: T.() -> Unit): T {
        if (condition) receiver.block()
        return receiver
    }

    override fun menu(block: MenuBuilder.() -> Unit): List<String> = Menu().apply(block).lines
}

private class CsvLineBuilder : CsvLine {
    private val cells = mutableListOf<String>()
    override fun cell(value: Any?) {
        cells += value?.toString().orEmpty()
    }

    fun build(): String = cells.joinToString(",")
}

private class Menu : MenuBuilder {
    val lines = mutableListOf<String>()
    override fun item(name: String, priceInCents: Int) {
        lines += "$name: ${priceInCents / 100}.${(priceInCents % 100).toString().padStart(2, '0')}"
    }
}
