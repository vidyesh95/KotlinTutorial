package exercises.ex03

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

abstract class Ex03Spec(base: String) : SpringExerciseSpec(base) {

    private fun submit(json: String): ResultActionsDsl = mockMvc.post("$base/reviews") {
        contentType = MediaType.APPLICATION_JSON
        content = json
    }

    @Test
    fun `03_1 invalid fields give 400 with field errors`() {
        submit("""{"bookId":0,"rating":6,"email":"not-an-email","text":"${"x".repeat(501)}"}""").andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.bookId") { exists() }
            jsonPath("$.errors.rating") { exists() }
            jsonPath("$.errors.email") { exists() }
            jsonPath("$.errors.text") { exists() }
        }
    }

    @Test
    fun `03_2 valid review is accepted`() {
        submit("""{"bookId":1,"rating":5,"email":"ada@example.com","text":"Loved it"}""").andExpect {
            status { isCreated() }
            jsonPath("$.bookId") { value(1) }
            jsonPath("$.rating") { value(5) }
            jsonPath("$.accepted") { value(true) }
        }
    }

    @Test
    fun `03_2 unknown book gives 404`() {
        submit("""{"bookId":999,"rating":3,"email":"ada@example.com"}""").andExpect {
            status { isNotFound() }
        }
    }

    @Test
    fun `03_3 spam gives 422 problem details`() {
        submit("""{"bookId":1,"rating":1,"email":"bot@example.com","text":"Buy SPAM now"}""").andExpect {
            status { isUnprocessableContent() }
            jsonPath("$.title") { value("Spam detected") }
            jsonPath("$.detail") { value("Review looks like spam") }
        }
    }
}

class Ex03SolutionTest : Ex03Spec("/solutions/ex03")

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec("/exercises/ex03")
