package exercises.ex03

import org.junit.jupiter.api.Tag
import solutions.ex03.Ex03Solution
import sqlkit.bookstoreDb
import sqlkit.exec
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex03Spec(private val impl: Ex03) {
    @Test
    fun `03_1 cheap books in stock`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("Collected Notes", "500"),
                    listOf("Pride and Prejudice", "799"),
                    listOf("I, Robot", "899"),
                    listOf("A Wizard of Earthsea", "999"),
                ),
                db.rows(impl.cheapBooksInStock),
            )
        }
    }

    @Test
    fun `03_2 second page of newest books`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("4", "The Left Hand of Darkness", "1969"),
                    listOf("13", "Dune Messiah", "1969"),
                    listOf("5", "A Wizard of Earthsea", "1968"),
                ),
                db.rows(impl.secondPageNewest),
            )
        }
    }

    @Test
    fun `03_3 price bands`() {
        bookstoreDb().use { db ->
            assertEquals(
                listOf(
                    listOf("The Hobbit", "standard"),
                    listOf("The Lord of the Rings", "premium"),
                    listOf("Designing Data-Intensive Applications", "premium"),
                ),
                db.rows(impl.priceBands),
            )
        }
    }

    @Test
    fun `03_4 restock only sold-out books`() {
        bookstoreDb().use { db ->
            db.exec(impl.restockSoldOut)
            assertEquals(listOf(listOf("3"), listOf("4"), listOf("13")), db.rows("SELECT id FROM books WHERE stock = 5 ORDER BY id"))
            assertEquals(listOf(listOf("12")), db.rows("SELECT stock FROM books WHERE id = 1"))
        }
    }
}

class Ex03SolutionTest : Ex03Spec(Ex03Solution)

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec(Ex03Exercise)
