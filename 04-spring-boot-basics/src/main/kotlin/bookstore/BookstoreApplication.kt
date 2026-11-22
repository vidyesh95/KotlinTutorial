package bookstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

/**
 * Lesson 01 — the whole app starts here.
 *
 * @SpringBootApplication = three things in one:
 *  • @SpringBootConfiguration  → this class is a @Configuration (can hold @Bean methods)
 *  • @EnableAutoConfiguration  → "look at my classpath and configure what you find"
 *                                 (JDBC driver? → DataSource. Web starter? → Tomcat + MVC. …)
 *  • @ComponentScan            → find @Component/@Service/@Repository/@RestController classes
 *                                 in THIS package (`bookstore`) and below
 *
 * @ConfigurationPropertiesScan → find @ConfigurationProperties classes (lesson 04).
 *
 * Scanning starts at THIS class's package by default. We list the exercise and solution
 * packages too, so their controllers/services become beans (see src/main/kotlin/exercises/README.md).
 */
@SpringBootApplication(scanBasePackages = ["bookstore", "exercises", "solutions"])
@ConfigurationPropertiesScan("bookstore", "exercises", "solutions")
class BookstoreApplication

fun main(args: Array<String>) {
    runApplication<BookstoreApplication>(*args)   // reified Kotlin helper (module 01, lesson 27)
}
