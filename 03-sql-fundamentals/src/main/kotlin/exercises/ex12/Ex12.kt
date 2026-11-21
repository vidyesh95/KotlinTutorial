package exercises.ex12

import java.sql.Connection

/*
 * Exercise 12 — Transactions                    (lesson: lesson12_transactions)
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex12*'
 *
 * The tests create this table (note: NO check constraint — YOU must check the balance):
 *   CREATE TABLE accounts (id INTEGER PRIMARY KEY, owner TEXT NOT NULL, balance_cents INTEGER NOT NULL)
 */

data class Transfer(val from: Int, val to: Int, val cents: Int)

interface Ex12 {
    /**
     * 12.1 — Run [block] in a transaction: commit if it returns normally, roll back and RETHROW if it
     * throws. Restore the connection's previous autoCommit setting in both cases.
     */
    fun <T> inTransaction(db: Connection, block: () -> T): T

    /**
     * 12.2 — Move [cents] from account [from] to account [to], atomically.
     * Throw IllegalArgumentException when [from] has too little money or either account doesn't
     * exist — and in that case NOTHING may change.
     */
    fun transfer(db: Connection, from: Int, to: Int, cents: Int)

    /** 12.3 — Apply all [transfers] in order, as ONE transaction: if any fails, none happen (rethrow the error). */
    fun transferAll(db: Connection, transfers: List<Transfer>)
}

object Ex12Exercise : Ex12 {
    override fun <T> inTransaction(db: Connection, block: () -> T): T = TODO("12.1")
    override fun transfer(db: Connection, from: Int, to: Int, cents: Int): Unit = TODO("12.2")
    override fun transferAll(db: Connection, transfers: List<Transfer>): Unit = TODO("12.3")
}
