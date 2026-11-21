package exercises.ex02

/*
 * Exercise 02 — Constraints and keys            (lesson: lesson02_constraints_and_keys)
 * Write SQL as Kotlin strings; tests run it on a fresh SQLite database (foreign keys ON).
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex02*'
 */
interface Ex02 {
    /**
     * 02.1 — CREATE TABLE `users`:
     *   id INTEGER primary key · email TEXT required and unique · age INTEGER (nullable) that must be >= 13 when present
     */
    val createUsers: String

    /**
     * 02.2 — CREATE TABLE `posts` (runs after 02.1):
     *   id INTEGER primary key · user_id INTEGER required, references users(id), posts are deleted with their user ·
     *   title TEXT required
     */
    val createPosts: String

    /**
     * 02.3 — CREATE TABLE `likes` (runs after 02.2): user_id and post_id, both required foreign keys
     * (deleted together with the user / the post), and the pair (user_id, post_id) is the primary key
     * so nobody can like the same post twice.
     */
    val createLikes: String
}

object Ex02Exercise : Ex02 {
    override val createUsers: String get() = TODO("02.1")
    override val createPosts: String get() = TODO("02.2")
    override val createLikes: String get() = TODO("02.3")
}
