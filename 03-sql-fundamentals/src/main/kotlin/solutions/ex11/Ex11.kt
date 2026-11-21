package solutions.ex11

import exercises.ex11.Book
import exercises.ex11.Ex11
import java.sql.Connection
import java.sql.ResultSet

private fun <T> Connection.query(sql: String, vararg params: Any?, map: (ResultSet) -> T): List<T> =
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, p -> statement.setObject(i + 1, p) }
        statement.executeQuery().use { rs -> buildList { while (rs.next()) add(map(rs)) } }
    }

private fun ResultSet.toBook() = Book(
    id = getLong("id"),
    title = getString("title"),
    priceCents = getInt("price_cents"),
    year = getInt("published_year").takeUnless { wasNull() },
)

object Ex11Solution : Ex11 {
    override fun findBooksByAuthor(db: Connection, authorName: String): List<Book> = db.query(
        "SELECT b.* FROM books b JOIN authors a ON a.id = b.author_id WHERE a.name = ? ORDER BY b.id",
        authorName,
    ) { it.toBook() }

    override fun findBook(db: Connection, id: Long): Book? =
        db.query("SELECT * FROM books WHERE id = ?", id) { it.toBook() }.singleOrNull()

    override fun insertAuthor(db: Connection, name: String, country: String?): Long =
        db.query("INSERT INTO authors (name, country) VALUES (?, ?) RETURNING id", name, country) { it.getLong("id") }.single()

    override fun raisePrices(db: Connection, categoryId: Int, percent: Int): Int =
        db.prepareStatement("UPDATE books SET price_cents = price_cents * (100 + ?) / 100 WHERE category_id = ?").use {
            it.setInt(1, percent)
            it.setInt(2, categoryId)
            it.executeUpdate()
        }
}
