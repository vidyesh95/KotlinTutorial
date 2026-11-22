package exercises.ex08

import bookstore.books.BookRepository
import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component

/*
 * Exercise 08 — Actuator and operations          (lesson: lessons/08-actuator-and-operations.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex08*'
 * See it live: bootRun, then http://localhost:8080/actuator/health/ex08Exercise
 */

/**
 * 08.1 — An inventory health check, published as component "ex08Exercise" (the bean name).
 * Return status UP with details:
 *   "totalBooks"  → number of books                                (books.count())
 *   "outOfStock"  → titles of books with stock 0, ordered by id    (books.findLowStock(0) gives stock <= 0)
 * …but status DOWN when MORE THAN HALF of all books are out of stock (shop can't really sell).
 *
 * The placeholder below returns UNKNOWN (not TODO()), because an exception here would make the
 * app's overall /actuator/health look broken until you solve it.
 */
@Component("ex08Exercise")
class Ex08InventoryHealthIndicator(private val books: BookRepository) : HealthIndicator {
    override fun health(): Health = Health.unknown().withDetail("todo", "exercise 08.1").build()
}
