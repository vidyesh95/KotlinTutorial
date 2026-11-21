package solutions.ex02

import exercises.ex02.Ex02

object Ex02Solution : Ex02 {
    override val createUsers = """
        CREATE TABLE users (
            id    INTEGER PRIMARY KEY,
            email TEXT NOT NULL UNIQUE,
            age   INTEGER CHECK (age >= 13)
        );
    """.trimIndent()

    override val createPosts = """
        CREATE TABLE posts (
            id      INTEGER PRIMARY KEY,
            user_id INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
            title   TEXT    NOT NULL
        );
    """.trimIndent()

    override val createLikes = """
        CREATE TABLE likes (
            user_id INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
            post_id INTEGER NOT NULL REFERENCES posts (id) ON DELETE CASCADE,
            PRIMARY KEY (user_id, post_id)
        );
    """.trimIndent()
}
