package bookstore.db

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import java.util.Properties
import kotlin.time.Instant

/*
 * Column adapters (lesson 02): convert between what SQLite stores and the Kotlin type
 * declared with `AS …` in the migrations.
 *
 *   SQLite storage        Kotlin type        adapter
 *   INTEGER (Long)   ⇄    Int                IntAdapter
 *   TEXT             ⇄    Instant            InstantAdapter (ISO-8601, e.g. 2026-01-01T00:00:00Z)
 *   TEXT             ⇄    OrderStatus        EnumColumnAdapter (stores the enum's name)
 *   INTEGER 0/1      ⇄    Boolean            built in, no adapter needed
 */

object IntAdapter : ColumnAdapter<Int, Long> {
    override fun decode(databaseValue: Long): Int = Math.toIntExact(databaseValue)   // throws instead of silently overflowing
    override fun encode(value: Int): Long = value.toLong()
}

object InstantAdapter : ColumnAdapter<Instant, String> {
    override fun decode(databaseValue: String): Instant = Instant.parse(databaseValue)
    override fun encode(value: Instant): String = value.toString()
}

/** The ONE place that knows how to build a BookstoreDatabase from any driver (lessons and Spring). */
fun createDatabase(driver: SqlDriver): BookstoreDatabase = BookstoreDatabase(
    driver = driver,
    booksAdapter = Books.Adapter(
        price_centsAdapter = IntAdapter,
        stockAdapter = IntAdapter,
        published_yearAdapter = IntAdapter,
        created_atAdapter = InstantAdapter,
    ),
    order_itemsAdapter = Order_items.Adapter(
        quantityAdapter = IntAdapter,
        unit_price_centsAdapter = IntAdapter,
    ),
    ordersAdapter = Orders.Adapter(
        statusAdapter = EnumColumnAdapter(),
        total_centsAdapter = IntAdapter,
        created_atAdapter = InstantAdapter,
    ),
    reviewsAdapter = Reviews.Adapter(
        ratingAdapter = IntAdapter,
        created_atAdapter = InstantAdapter,
    ),
)

/**
 * For lessons: a fresh in-memory database with every migration applied, INCLUDING the seed data.
 * Without Spring/Flyway we let SQLDelight run the migrations itself.
 *
 * ⚠️ `Schema.create(driver)` would only create the final TABLES (no INSERTs from V2/V3).
 * `Schema.migrate(driver, 1, version)` replays every .sqm file in order: V1, V2, V3 (lesson 03).
 */
fun inMemoryBookstore(): Pair<SqlDriver, BookstoreDatabase> {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY, Properties().apply { put("foreign_keys", "true") })
    BookstoreDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = BookstoreDatabase.Schema.version)
    return driver to createDatabase(driver)
}
