package bookstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/** Lesson 05 — the bookstore API again, but every query comes from a compiler-checked .sq file. */
@SpringBootApplication
class SqlDelightBookstoreApplication

fun main(args: Array<String>) {
    runApplication<SqlDelightBookstoreApplication>(*args)
}
