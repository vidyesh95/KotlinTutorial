package solutions.ex02

import app.cash.sqldelight.ColumnAdapter
import exercises.ex02.Isbn

object IsbnAdapterSolution : ColumnAdapter<Isbn, String> {
    override fun decode(databaseValue: String): Isbn = Isbn(databaseValue)
    override fun encode(value: Isbn): String = value.value
}
