package exercises.ex04

import bookstore.db.BookstoreDatabase
import kotlinx.coroutines.flow.Flow
import kotlin.coroutines.CoroutineContext

/*
 * Exercise 04 — transactions and flows                 (lesson: lesson04_transactions_and_flows)
 * Use `db.ex04ExerciseQueries.addStock(quantity, id)` (given in Ex04Exercise.sq) and `db.booksQueries`.
 * Check: ./gradlew :05-sqldelight:exercises --tests 'exercises.ex04*'
 */
interface Ex04 {
    /**
     * 04.1 — Add stock for every bookId → quantity. ALL OR NOTHING: if any bookId doesn't exist
     * (addStock changed 0 rows), throw NoSuchElementException and leave every stock unchanged.
     * Hint: `db.transaction { … }` — an exception thrown inside rolls everything back.
     */
    fun restock(db: BookstoreDatabase, quantities: Map<Long, Int>)

    /**
     * 04.2 — Move [quantity] copies' worth of stock from book [fromId] to book [toId].
     * If the source doesn't have enough, change nothing and return false; otherwise true.
     * Hint: `db.transactionWithResult { … rollback(false) … }` + booksQueries.takeStock.
     */
    fun transferStock(db: BookstoreDatabase, fromId: Long, toId: Long, quantity: Int): Boolean

    /**
     * 04.3 — A Flow of the titles of books with stock <= [threshold] (order: stock, then id), which
     * emits again whenever the books table changes. Hint: booksQueries.lowStock(...).asFlow().mapToList(context).
     */
    fun lowStockTitles(db: BookstoreDatabase, threshold: Int, context: CoroutineContext): Flow<List<String>>
}

object Ex04Exercise : Ex04 {
    override fun restock(db: BookstoreDatabase, quantities: Map<Long, Int>): Unit = TODO("04.1")
    override fun transferStock(db: BookstoreDatabase, fromId: Long, toId: Long, quantity: Int): Boolean = TODO("04.2")
    override fun lowStockTitles(db: BookstoreDatabase, threshold: Int, context: CoroutineContext): Flow<List<String>> =
        TODO("04.3")
}
