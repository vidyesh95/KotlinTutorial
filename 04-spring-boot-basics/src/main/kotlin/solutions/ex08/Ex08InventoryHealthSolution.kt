package solutions.ex08

import bookstore.books.BookRepository
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

@Component("ex08Solution")
class Ex08InventoryHealthSolution(private val books: BookRepository) : HealthIndicator {
    override fun health(): Health {
        val total = books.count()
        val outOfStock = books.findLowStock(0).sortedBy { it.id }.map { it.title }
        val builder = if (outOfStock.size * 2 > total) Health.down() else Health.up()
        return builder
            .withDetail("totalBooks", total)
            .withDetail("outOfStock", outOfStock)
            .build()
    }
}
