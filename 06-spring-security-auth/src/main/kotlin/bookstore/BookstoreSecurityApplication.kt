package bookstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["bookstore", "exercises", "solutions"])
@ConfigurationPropertiesScan("bookstore", "exercises", "solutions")
class BookstoreSecurityApplication

fun main(args: Array<String>) {
    runApplication<BookstoreSecurityApplication>(*args)
}
