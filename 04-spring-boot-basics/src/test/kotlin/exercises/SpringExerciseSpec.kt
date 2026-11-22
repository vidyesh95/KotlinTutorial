package exercises

import bookstore.BookstoreApplication
import bookstore.TestClock
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

/**
 * Base class for exercise specs: the whole app, a fresh SQLite file per test class,
 * a fixed clock, and MockMvc. Spring test annotations are inherited by subclasses.
 *
 * [base] is "/exercises/exNN" (your code) or "/solutions/exNN" (reference answer).
 *
 * `classes = [BookstoreApplication::class]`: Spring normally finds the app class by searching
 * the test's package and its parents. These tests live in `exercises`, not `bookstore`, so we say it.
 */
@SpringBootTest(classes = [BookstoreApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClock::class)
@DirtiesContext
abstract class SpringExerciseSpec(protected val base: String) {
    @Autowired
    protected lateinit var mockMvc: MockMvc
}
