package exercises.ex04

import org.junit.jupiter.api.Tag
import solutions.ex04.Ex04Solution
import sqlkit.bookstoreDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex04Spec(private val impl: Ex04) {
    @Test
    fun `04_1 books by UK authors`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("Designing Data-Intensive Applications", "Martin Kleppmann"),
                    listOf("Emma", "Jane Austen"),
                    listOf("Pride and Prejudice", "Jane Austen"),
                    listOf("SPQR", "Mary Beard"),
                    listOf("The Hobbit", "J.R.R. Tolkien"),
                    listOf("The Lord of the Rings", "J.R.R. Tolkien"),
                ),
                db.rows(impl.ukBooks),
            )
        }
    }

    @Test
    fun `04_2 customers without orders`() {
        bookstoreDb().use { db ->
            assertEquals(listOf(listOf("Edsger Dijkstra")), db.rows(impl.customersWithoutOrders))
        }
    }

    @Test
    fun `04_3 categories with parent`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("Classics", "Fiction"),
                    listOf("Databases", "Programming"),
                    listOf("Fantasy", "Fiction"),
                    listOf("Fiction", "top level"),
                    listOf("History", "Non-fiction"),
                    listOf("Non-fiction", "top level"),
                    listOf("Programming", "Non-fiction"),
                    listOf("Science Fiction", "Fiction"),
                ),
                db.rows(impl.categoriesWithParent),
            )
        }
    }

    @Test
    fun `04_4 what Ada bought`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf("Designing Data-Intensive Applications", "Dune", "SPQR", "The Hobbit").map { listOf(it) },
                db.rows(impl.adaPurchases),
            )
        }
    }
}

class Ex04SolutionTest : Ex04Spec(Ex04Solution)

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec(Ex04Exercise)
