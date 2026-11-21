package exercises.ex10

import org.junit.jupiter.api.Tag
import solutions.ex10.Ex10Solution
import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.rows
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

abstract class Ex10Spec(private val impl: Ex10) {
    private fun library(block: (Connection) -> Unit) {
        memoryDb().use { db ->
            db.exec(impl.createLibrarySchema)
            db.exec(
                """
                INSERT INTO members (id, email, name) VALUES (1, 'ada@example.com', 'Ada'), (2, 'bob@example.com', 'Bob');
                INSERT INTO books (id, title, isbn) VALUES (1, 'Dune', 'isbn-1'), (2, 'Emma', 'isbn-2'), (3, 'SPQR', 'isbn-3');
                INSERT INTO tags (id, name) VALUES (1, 'sci-fi'), (2, 'classic'), (3, 'bestseller');
                INSERT INTO book_tags (book_id, tag_id) VALUES (1, 1), (1, 3), (2, 2);
                INSERT INTO loans (member_id, book_id, loaned_at, returned_at) VALUES
                    (1, 1, '2026-01-03', NULL),
                    (2, 2, '2026-01-01', '2026-01-02'),
                    (2, 3, '2026-01-02', NULL);
                """,
            )
            block(db)
        }
    }

    @Test
    fun `10_1 schema accepts valid data and enforces the rules`() {
        library { db ->
            assertFails("duplicate member email") { db.exec("INSERT INTO members (email, name) VALUES ('ada@example.com', 'X')") }
            assertFails("duplicate isbn") { db.exec("INSERT INTO books (title, isbn) VALUES ('X', 'isbn-1')") }
            assertFails("book title required") { db.exec("INSERT INTO books (isbn) VALUES ('isbn-9')") }
            assertFails("loan of unknown member") { db.exec("INSERT INTO loans (member_id, book_id, loaned_at) VALUES (99, 1, '2026-01-05')") }
            assertFails("loan of unknown book") { db.exec("INSERT INTO loans (member_id, book_id, loaned_at) VALUES (1, 99, '2026-01-05')") }
            assertFails("loaned_at required") { db.exec("INSERT INTO loans (member_id, book_id) VALUES (1, 1)") }
            assertFails("same tag twice on a book") { db.exec("INSERT INTO book_tags (book_id, tag_id) VALUES (1, 1)") }
            assertFails("duplicate tag name") { db.exec("INSERT INTO tags (name) VALUES ('sci-fi')") }
        }
    }

    @Test
    fun `10_2 open loans`() {
        library { db ->
            assertEquals(listOf(listOf<String?>("Bob", "SPQR"), listOf("Ada", "Dune")), db.rows(impl.openLoans))
        }
    }

    @Test
    fun `10_3 books with tags`() {
        library { db ->
            val expected = listOf(
                listOf<String?>("Dune", "bestseller,sci-fi"),
                listOf("Emma", "classic"),
                listOf("SPQR", null),
            )
            assertEquals(expected, db.rows(impl.booksWithTags))
        }
    }
}

class Ex10SolutionTest : Ex10Spec(Ex10Solution)

@Tag("exercise")
class Ex10ExerciseTest : Ex10Spec(Ex10Exercise)
