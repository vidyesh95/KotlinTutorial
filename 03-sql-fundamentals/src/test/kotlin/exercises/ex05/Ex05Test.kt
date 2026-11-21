package exercises.ex05

import org.junit.jupiter.api.Tag
import solutions.ex05.Ex05Solution
import sqlkit.bookstoreDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex05Spec(private val impl: Ex05) {
    @Test
    fun `05_1 books per category`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("Science Fiction", "5"),
                    listOf("Fantasy", "3"),
                    listOf("Classics", "2"),
                    listOf("Databases", "1"),
                    listOf("History", "1"),
                    listOf("Programming", "1"),
                ),
                db.rows(impl.booksPerCategory),
            )
        }
    }

    @Test
    fun `05_2 well reviewed books`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("Designing Data-Intensive Applications", "2", "5.0"),
                    listOf("Dune", "2", "4.5"),
                ),
                db.rows(impl.wellReviewedBooks),
            )
        }
    }

    @Test
    fun `05_3 revenue per status`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("cancelled", "2999"),
                    listOf("paid", "17893"),
                    listOf("pending", "5898"),
                    listOf("shipped", "13688"),
                ),
                db.rows(impl.revenuePerStatus),
            )
        }
    }

    @Test
    fun `05_4 loyal customers`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf("Ada Lovelace", "Alan Turing", "Margaret Hamilton").map { listOf(it) },
                db.rows(impl.loyalCustomers),
            )
        }
    }
}

class Ex05SolutionTest : Ex05Spec(Ex05Solution)

@Tag("exercise")
class Ex05ExerciseTest : Ex05Spec(Ex05Exercise)
