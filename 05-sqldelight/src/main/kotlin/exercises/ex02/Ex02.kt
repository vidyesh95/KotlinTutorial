package exercises.ex02

import app.cash.sqldelight.ColumnAdapter

/*
 * Exercise 02 (Kotlin part) — column adapters          (lesson: lesson02_queries_and_types)
 * The SQL part is in src/main/sqldelight/exercises/ex02/Ex02Exercise.sq.
 * Check: ./gradlew :05-sqldelight:exercises --tests 'exercises.ex02*'
 */

/** An ISBN-13: exactly 13 digits. A value class, so it costs nothing at runtime. */
@JvmInline
value class Isbn(val value: String) {
    init {
        require(value.length == 13 && value.all(Char::isDigit)) { "Invalid ISBN: $value" }
    }
}

/**
 * 02.5 — A ColumnAdapter that stores an [Isbn] as TEXT. With it, a column declared
 * `isbn TEXT AS Isbn NOT NULL` would give you `Isbn` instead of `String` in Kotlin.
 * decode must REJECT invalid text (the Isbn constructor already does — just use it).
 */
object IsbnAdapterExercise : ColumnAdapter<Isbn, String> {
    override fun decode(databaseValue: String): Isbn = TODO("02.5 decode")
    override fun encode(value: Isbn): String = TODO("02.5 encode")
}
