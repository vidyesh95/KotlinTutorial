package bookstore.ops

import bookstore.books.BookRepository
import bookstore.config.BookstoreProperties
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

/**
 * Lesson 08 — a custom health check, shown at GET /actuator/health under "lowStock".
 *
 * Health is what load balancers / Kubernetes ask: "is this instance OK to receive traffic?"
 * The database check ("db") comes for free from Spring Boot. This one adds a business signal.
 */
@Component("lowStock")
class LowStockHealthIndicator(
    private val books: BookRepository,
    private val props: BookstoreProperties,
) : HealthIndicator {

    override fun health(): Health {
        val low = books.findLowStock(props.lowStockThreshold)
        return Health.up()   // low stock is a warning, not an outage → still UP
            .withDetail("threshold", props.lowStockThreshold)
            .withDetail("lowStockBooks", low.map { "${it.title} (${it.stock})" })
            .build()
    }
}
