package solutions.ex04

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import bookstore.db.BookstoreDatabase
import exercises.ex04.Ex04
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.coroutines.CoroutineContext

object Ex04Solution : Ex04 {
    override fun restock(db: BookstoreDatabase, quantities: Map<Long, Int>) {
        db.transaction {
            for ((id, quantity) in quantities) {
                val changed = db.ex04SolutionQueries.addStock(quantity, id).value
                if (changed == 0L) throw NoSuchElementException("Book $id not found")   // → rollback
            }
        }
    }

    override fun transferStock(db: BookstoreDatabase, fromId: Long, toId: Long, quantity: Int): Boolean =
        db.transactionWithResult {
            if (db.booksQueries.takeStock(quantity = quantity, id = fromId).value == 0L) rollback(false)
            db.ex04SolutionQueries.addStock(quantity, toId)
            true
        }

    override fun lowStockTitles(db: BookstoreDatabase, threshold: Int, context: CoroutineContext): Flow<List<String>> =
        db.booksQueries.lowStock(threshold).asFlow().mapToList(context).map { rows -> rows.map { it.title } }
}
