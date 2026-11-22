package exercises.ex01

import bookstore.books.BookRepository
import bookstore.config.BookstoreProperties
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/*
 * Exercise 01 — Hello, Spring Boot            (lesson: lessons/01-hello-spring-boot.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex01*'
 * Or start the app (bootRun) and open http://localhost:8080/exercises/ex01/hello?name=Ada
 */

data class Ex01Message(val message: String)

data class Ex01Info(val app: String, val currency: String, val bookCount: Long)

/** Spring builds this controller and injects both constructor parameters for you (module 02!). */
@RestController
@RequestMapping("/exercises/ex01")
class Ex01HelloController(
    private val props: BookstoreProperties,
    private val books: BookRepository,
) {
    /**
     * 01.1 — GET /exercises/ex01/hello?name=Ada → {"message": "Hello, Ada! Welcome to Kotlin Books"}
     * The shop name comes from props.name. Without ?name= greet "stranger" (the default is already set).
     */
    @GetMapping("/hello")
    fun hello(@RequestParam(defaultValue = "stranger") name: String): Ex01Message = TODO("01.1")

    /**
     * 01.2 — GET /exercises/ex01/info → {"app": "Kotlin Books", "currency": "EUR", "bookCount": 5}
     * bookCount = how many books are in the database (BookRepository has a method for that).
     */
    @GetMapping("/info")
    fun info(): Ex01Info = TODO("01.2")
}
