package exercises.ex02

import org.junit.jupiter.api.Tag
import solutions.ex02.Ex02Solution
import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.rows
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

abstract class Ex02Spec(private val impl: Ex02) {
    private fun withSchema(upTo: Int, block: (Connection) -> Unit) {
        memoryDb().use { db ->
            db.exec(impl.createUsers)
            if (upTo >= 2) db.exec(impl.createPosts)
            if (upTo >= 3) db.exec(impl.createLikes)
            block(db)
        }
    }

    private fun Connection.count(table: String) = rows("SELECT count(*) FROM $table").single().single()

    @Test
    fun `02_1 users rules`() {
        withSchema(1) { db ->
            db.exec("INSERT INTO users (email, age) VALUES ('ada@example.com', 36), ('kid@example.com', NULL)")
            assertFails("email must be unique") { db.exec("INSERT INTO users (email) VALUES ('ada@example.com')") }
            assertFails("email must be required") { db.exec("INSERT INTO users (age) VALUES (20)") }
            assertFails("age must be >= 13") { db.exec("INSERT INTO users (email, age) VALUES ('tiny@example.com', 8)") }
            assertEquals("2", db.count("users"))
        }
    }

    @Test
    fun `02_2 posts belong to existing users and cascade`() {
        withSchema(2) { db ->
            db.exec("INSERT INTO users (id, email) VALUES (1, 'ada@example.com')")
            db.exec("INSERT INTO posts (user_id, title) VALUES (1, 'Hello'), (1, 'Again')")
            assertFails("user 99 does not exist") { db.exec("INSERT INTO posts (user_id, title) VALUES (99, 'Ghost')") }
            assertFails("title is required") { db.exec("INSERT INTO posts (user_id) VALUES (1)") }
            db.exec("DELETE FROM users WHERE id = 1")
            assertEquals("0", db.count("posts"))
        }
    }

    @Test
    fun `02_3 likes are unique per user and post`() {
        withSchema(3) { db ->
            db.exec("INSERT INTO users (id, email) VALUES (1, 'ada@example.com'), (2, 'bob@example.com')")
            db.exec("INSERT INTO posts (id, user_id, title) VALUES (10, 1, 'Hello')")
            db.exec("INSERT INTO likes (user_id, post_id) VALUES (1, 10), (2, 10)")
            assertFails("same like twice") { db.exec("INSERT INTO likes (user_id, post_id) VALUES (2, 10)") }
            assertFails("post must exist") { db.exec("INSERT INTO likes (user_id, post_id) VALUES (1, 99)") }
            db.exec("DELETE FROM posts WHERE id = 10")
            assertEquals("0", db.count("likes"))
        }
    }
}

class Ex02SolutionTest : Ex02Spec(Ex02Solution)

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec(Ex02Exercise)
