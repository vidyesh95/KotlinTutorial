package exercises

import bookstore.BookstoreSecurityApplication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest(classes = [BookstoreSecurityApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
abstract class SpringSecurityExerciseSpec(protected val base: String) {
    @Autowired
    protected lateinit var mockMvc: MockMvc
}
