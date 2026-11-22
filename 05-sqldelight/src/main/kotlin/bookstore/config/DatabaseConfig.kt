package bookstore.config

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.asJdbcDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.createDatabase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy
import javax.sql.DataSource
import kotlin.time.Clock

/**
 * Lesson 05 — plugging SQLDelight into Spring.
 *
 * Spring Boot builds the DataSource (HikariCP pool) and runs Flyway on it. We hand SQLDelight
 * that SAME pool, wrapped in TransactionAwareDataSourceProxy:
 *
 *   @Transactional method ─▶ Spring opens connection C and starts a transaction
 *        └─ booksQueries.takeStock(…) ─▶ driver asks the proxy for a connection ─▶ gets C ✔
 *        └─ ordersQueries.insertOrder(…) ─▶ gets C again ✔  (same transaction!)
 *   method returns → COMMIT · method throws → ROLLBACK (all SQLDelight writes undone)
 *
 * Without the proxy, SQLDelight would take a DIFFERENT pooled connection and its writes
 * would commit on their own, outside Spring's transaction.
 *
 * ⚠️ Inside Spring code use @Transactional, not SQLDelight's own `database.transaction {}` —
 * one transaction manager at a time.
 */
@Configuration(proxyBeanMethods = false)
class DatabaseConfig {

    @Bean
    fun sqlDriver(dataSource: DataSource): SqlDriver = TransactionAwareDataSourceProxy(dataSource).asJdbcDriver()

    @Bean
    fun bookstoreDatabase(driver: SqlDriver): BookstoreDatabase = createDatabase(driver)

    @Bean
    fun clock(): Clock = Clock.System
}
