package sqlkit

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.SQLException

/*
 * Small helpers used by every SQL lesson. You don't need to understand this
 * file to do the lessons — but by lesson 11 (JDBC from Kotlin) you will.
 */

/** A fresh, empty, in-memory SQLite database. It disappears when the connection closes. */
fun memoryDb(): Connection =
    DriverManager.getConnection("jdbc:sqlite::memory:").also { it.enableForeignKeys() }

/** An in-memory database pre-filled with the shared bookstore data (resources/sql/bookstore.sql). */
fun bookstoreDb(): Connection = memoryDb().also { it.runScript(resourceText("/sql/bookstore.sql"), echo = false) }

/**
 * ⚠️ SQLite quirk: foreign keys are NOT enforced unless you switch them on, per connection.
 * (Postgres always enforces them.)
 */
fun Connection.enableForeignKeys() {
    createStatement().use { it.execute("PRAGMA foreign_keys = ON") }
}

fun resourceText(path: String): String =
    object {}.javaClass.getResourceAsStream(path)?.bufferedReader()?.readText()
        ?: error("Resource not found: $path")

/**
 * Runs a lesson file from `src/main/resources/sql/`, printing a transcript:
 *  - `-- §…` comment lines are printed as section headers
 *  - every query that returns rows is echoed and its result printed as a table
 *  - INSERT/UPDATE/DELETE are echoed with the number of rows changed
 *  - a statement preceded by `-- expect error` is allowed to fail; the error is printed
 */
fun runSqlLesson(fileName: String, withBookstore: Boolean = false) {
    val db = if (withBookstore) bookstoreDb() else memoryDb()
    db.use { it.runScript(resourceText("/sql/$fileName"), echo = true) }
}

fun Connection.runScript(script: String, echo: Boolean) {
    for (item in parseScript(script)) {
        when (item) {
            is ScriptItem.Header -> if (echo) println(item.text)
            is ScriptItem.Statement -> execute(item, echo)
        }
    }
}

private fun Connection.execute(item: ScriptItem.Statement, echo: Boolean) {
    val sql = item.sql
    try {
        createStatement().use { statement ->
            val hasRows = statement.execute(sql)
            if (!echo) return
            if (hasRows) {
                println(echoOf(sql))
                statement.resultSet.use { println(it.toTable()) }
                println()
            } else if (sql.firstWord() in setOf("INSERT", "UPDATE", "DELETE", "REPLACE")) {
                val n = statement.updateCount
                println(echoOf(sql))
                println("→ $n row${if (n == 1) "" else "s"} ${pastTense(sql.firstWord())}")
                println()
            }
        }
    } catch (e: SQLException) {
        if (!item.expectError) throw IllegalStateException("SQL failed: $sql\n${e.message}", e)
        if (echo) {
            println(echoOf(sql))
            println("✗ error: ${e.message}")
            println()
        }
    }
}

private fun pastTense(verb: String) = when (verb) {
    "INSERT", "REPLACE" -> "inserted"
    "UPDATE" -> "updated"
    else -> "deleted"
}

private fun String.firstWord() = trimStart().substringBefore(' ').substringBefore('\n').uppercase()

private fun echoOf(sql: String) =
    sql.lines().joinToString("\n") { line -> "sql> $line" }

// ── Pretty tables ──────────────────────────────────────────────────────────

/** Formats a result set as an aligned text table, e.g.
 * ```
 * id | title
 * ---+------
 * 1  | Dune
 * (1 row)
 * ```
 */
fun ResultSet.toTable(): String {
    val columns = (1..metaData.columnCount).map { metaData.getColumnLabel(it) }
    val rows = buildList {
        while (next()) add(columns.indices.map { i -> getString(i + 1) ?: "NULL" })
    }
    val widths = columns.indices.map { i -> maxOf(columns[i].length, rows.maxOfOrNull { it[i].length } ?: 0) }
    fun line(cells: List<String>) =
        cells.mapIndexed { i, c -> c.padEnd(widths[i]) }.joinToString(" | ").trimEnd()
    return buildString {
        appendLine(line(columns))
        appendLine(widths.joinToString("-+-") { "-".repeat(it) })
        rows.forEach { appendLine(line(it)) }
        append("(${rows.size} row${if (rows.size == 1) "" else "s"})")
    }
}

/** Run a query and print it as a table (handy in Kotlin lessons). Parameters bind to `?` in order. */
fun Connection.printQuery(sql: String, vararg params: Any?) {
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, p -> statement.setObject(i + 1, p) }
        statement.executeQuery().use { println(it.toTable()) }
    }
}

/** Run a query and return every row as a list of strings (NULL → null). Used by exercise tests. */
fun Connection.rows(sql: String, vararg params: Any?): List<List<String?>> =
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, p -> statement.setObject(i + 1, p) }
        statement.executeQuery().use { rs ->
            val count = rs.metaData.columnCount
            buildList { while (rs.next()) add((1..count).map { rs.getString(it) }) }
        }
    }

/** Run any statement(s) without printing (DDL, inserts). */
fun Connection.exec(sql: String) {
    runScript(sql, echo = false)
}

// ── Script parsing ─────────────────────────────────────────────────────────

sealed interface ScriptItem {
    data class Header(val text: String) : ScriptItem
    data class Statement(val sql: String, val expectError: Boolean) : ScriptItem
}

/**
 * Splits a script into statements. A statement ends at a line ending with `;`
 * (ignoring a trailing `-- comment`), except inside `CREATE TRIGGER … END;`.
 * The final statement may omit its `;`.
 * Full-line comments are dropped, except `-- §` headers and `-- expect error`.
 */
fun parseScript(script: String): List<ScriptItem> {
    val items = mutableListOf<ScriptItem>()
    val current = mutableListOf<String>()
    var expectError = false

    for (raw in script.lines()) {
        val trimmed = raw.trim()
        if (current.isEmpty()) {
            when {
                trimmed.startsWith("-- §") -> { items += ScriptItem.Header(trimmed.removePrefix("-- ")); continue }
                trimmed == "-- expect error" -> { expectError = true; continue }
                trimmed.isEmpty() || trimmed.startsWith("--") -> continue
            }
        } else if (trimmed.startsWith("--")) {
            continue
        }
        current += raw.trimEnd()
        val code = trimmed.substringBefore(" --").trimEnd()
        val inTrigger = current.first().trim().uppercase().startsWith("CREATE TRIGGER")
        val ends = if (inTrigger) code.equals("END;", ignoreCase = true) else code.endsWith(";")
        if (ends) {
            items += ScriptItem.Statement(current.joinToString("\n").trimEnd().removeSuffix(";"), expectError)
            current.clear()
            expectError = false
        }
    }
    if (current.isNotEmpty()) {   // last statement without a ';' is fine too
        items += ScriptItem.Statement(current.joinToString("\n").trimEnd(), expectError)
    }
    return items
}
