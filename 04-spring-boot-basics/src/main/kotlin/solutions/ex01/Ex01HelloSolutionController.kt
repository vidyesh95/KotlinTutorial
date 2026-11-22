package solutions.ex01

import bookstore.books.BookRepository
import bookstore.config.BookstoreProperties
import exercises.ex01.Ex01Info
import exercises.ex01.Ex01Message
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/solutions/ex01")
class Ex01HelloSolutionController(
    private val props: BookstoreProperties,
    private val books: BookRepository,
) {
    @GetMapping("/hello")
    fun hello(@RequestParam(defaultValue = "stranger") name: String) =
        Ex01Message("Hello, $name! Welcome to ${props.name}")

    @GetMapping("/info")
    fun info() = Ex01Info(app = props.name, currency = props.currency, bookCount = books.count())
}
