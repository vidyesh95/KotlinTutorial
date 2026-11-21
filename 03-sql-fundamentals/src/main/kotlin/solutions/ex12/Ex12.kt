package solutions.ex12

import exercises.ex12.Ex12
import exercises.ex12.Transfer
import java.sql.Connection

object Ex12Solution : Ex12 {
    override fun <T> inTransaction(db: Connection, block: () -> T): T {
        val previous = db.autoCommit
        db.autoCommit = false
        try {
            return block().also { db.commit() }
        } catch (e: Throwable) {
            db.rollback()
            throw e
        } finally {
            db.autoCommit = previous
        }
    }

    override fun transfer(db: Connection, from: Int, to: Int, cents: Int) = inTransaction(db) { move(db, from, to, cents) }

    override fun transferAll(db: Connection, transfers: List<Transfer>) = inTransaction(db) {
        transfers.forEach { move(db, it.from, it.to, it.cents) }
    }

    /** The two updates, with checks. Must run inside a transaction. */
    private fun move(db: Connection, from: Int, to: Int, cents: Int) {
        val balance = db.prepareStatement("SELECT balance_cents FROM accounts WHERE id = ?").use {
            it.setInt(1, from)
            it.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else null }
        } ?: throw IllegalArgumentException("account $from does not exist")
        require(balance >= cents) { "account $from has only $balance cents" }
        add(db, from, -cents)
        add(db, to, +cents)
    }

    private fun add(db: Connection, id: Int, cents: Int) {
        db.prepareStatement("UPDATE accounts SET balance_cents = balance_cents + ? WHERE id = ?").use {
            it.setInt(1, cents)
            it.setInt(2, id)
            require(it.executeUpdate() == 1) { "account $id does not exist" }
        }
    }
}
