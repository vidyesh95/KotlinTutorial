package exercises.ex01

/*
 * Exercise 01 — Tables and types              (lesson: lesson01_tables_and_types)
 *
 * In SQL exercises you write SQL as Kotlin strings (raw strings """…""" are easiest).
 * Tests run your SQL against a real SQLite database. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex01*'
 * Tip: try your SQL in a lesson file or the IntelliJ Database tab first.
 */
interface Ex01 {
    /**
     * 01.1 — CREATE TABLE `products` as a STRICT table with columns:
     *   id INTEGER primary key · name TEXT, required · price_cents INTEGER, required ·
     *   in_stock INTEGER, required, default 1
     */
    val createProducts: String

    /** 01.2 — Add a nullable TEXT column `sku` to `products` (runs after 01.1). */
    val addSkuColumn: String

    /**
     * 01.3 — On the bookstore database: every book's `title` and `published_year`, but show the
     * text 'unknown' when the year is NULL. Order by id. (2 columns)
     */
    val booksWithYear: String
}

object Ex01Exercise : Ex01 {
    override val createProducts: String get() = TODO("01.1")
    override val addSkuColumn: String get() = TODO("01.2")
    override val booksWithYear: String get() = TODO("01.3")
}
