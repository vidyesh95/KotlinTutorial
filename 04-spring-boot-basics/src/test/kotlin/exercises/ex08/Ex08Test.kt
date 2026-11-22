package exercises.ex08

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch

/**
 * [component] is the health component name: "ex08Exercise" or "ex08Solution".
 * One test empties stock, so every test gets a fresh app + database (AFTER_EACH_TEST_METHOD).
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
abstract class Ex08Spec(private val component: String) : SpringExerciseSpec("/actuator/health/$component") {

    @Test
    fun `08_1 up with inventory details`() {
        mockMvc.get(base).andExpect {
            status { isOk() }
            jsonPath("$.status") { value("UP") }
            jsonPath("$.details.totalBooks") { value(5) }
            jsonPath("$.details.outOfStock[0]") { value("Emma") }
            jsonPath("$.details.outOfStock.length()") { value(1) }
        }
    }

    @Test
    fun `08_1 down when most books are out of stock`() {
        for (id in 1..3) {
            mockMvc.patch("/api/books/$id") {
                contentType = MediaType.APPLICATION_JSON
                content = """{"stock":0}"""
            }.andExpect { status { isOk() } }
        }
        mockMvc.get(base).andExpect {
            status { isServiceUnavailable() }     // DOWN → HTTP 503, what load balancers look for
            jsonPath("$.status") { value("DOWN") }
            jsonPath("$.details.outOfStock.length()") { value(4) }
        }
    }
}

class Ex08SolutionTest : Ex08Spec("ex08Solution")

@Tag("exercise")
class Ex08ExerciseTest : Ex08Spec("ex08Exercise")
