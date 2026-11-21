package exercises.ex11

import java.sql.Connection

/*
 * Exercise 11 — JDBC from Kotlin                (lesson: lesson11_jdbc_from_kotlin)
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex11*'
 * All functions receive a connection to the bookstore database. Use PreparedStatement + `?` —
 * the tests WILL try SQL injection.
 */

data class Book(val id: Long, val title: String, val priceCents: Int, val year: Int?)

interface Ex11 {
    /** 11.1 — Books written by the author with exactly this [authorName], ordered by id. */
    fun findBooksByAuthor(db: Connection, authorName: String): List<Book>

    /** 11.2 — The book with this id, or null. `year` must be null when published_year is NULL (not 0!). */
    fun findBook(db: Connection, id: Long): Book?

    /** 11.3 — Insert an author (country may be null) and return the NEW id. */
    fun insertAuthor(db: Connection, name: String, country: String?): Long

    /** 11.4 — Raise the price of every book in [categoryId] by [percent] % (integer cents, rounded down). Return how many rows changed. */
    fun raisePrices(db: Connection, categoryId: Int, percent: Int): Int
}

object Ex11Exercise : Ex11 {
    override fun findBooksByAuthor(db: Connection, authorName: String): List<Book> = TODO("11.1")
    override fun findBook(db: Connection, id: Long): Book? = TODO("11.2")
    override fun insertAuthor(db: Connection, name: String, country: String?): Long = TODO("11.3")
    override fun raisePrices(db: Connection, categoryId: Int, percent: Int): Int = TODO("11.4")
}
