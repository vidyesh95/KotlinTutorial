package bookstore.config

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Lesson 04 — type-safe configuration.
 *
 * Binds everything under `bookstore:` in application.yml to this data class:
 *   bookstore.low-stock-threshold  →  lowStockThreshold   ("relaxed binding": kebab-case → camelCase)
 * Override with env var BOOKSTORE_LOW_STOCK_THRESHOLD=10 or --bookstore.low-stock-threshold=10.
 * Inject it like any other bean: `class X(private val props: BookstoreProperties)`.
 */
@ConfigurationProperties(prefix = "bookstore")
data class BookstoreProperties(
    val name: String,
    val currency: String = "EUR",
    val maxPageSize: Int = 50,
    val lowStockThreshold: Int = 3,
)
