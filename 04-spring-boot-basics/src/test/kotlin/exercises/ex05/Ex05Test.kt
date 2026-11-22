package exercises.ex05

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/** Tests in one class share a database, so each test uses different books (order-independent). */
abstract class Ex05Spec(base: String) : SpringExerciseSpec(base) {

    private fun review(bookId: Long, rating: Int) {
        mockMvc.post("$base/books/$bookId/reviews") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"rating":$rating,"text":"r$rating"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.id") { isNumber() }
        }
    }

    @Test
    fun `05_1 and 05_2 average rating per book`() {
        mockMvc.get("$base/books/2/rating").andExpect {
            status { isOk() }
            jsonPath("$.count") { value(0) }
            jsonPath("$.average") { doesNotExist() }   // Jackson omits… or writes null; both mean "no value"
        }
        review(2, 5)
        review(2, 4)
        mockMvc.get("$base/books/2/rating").andExpect {
            jsonPath("$.average") { value(4.5) }
            jsonPath("$.count") { value(2) }
        }
    }

    @Test
    fun `05_3 top rated books`() {
        review(3, 5)
        review(5, 5)
        review(5, 5)
        review(1, 2)
        mockMvc.get("$base/top-rated?limit=2").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(2) }
            jsonPath("$[0].title") { value("Designing Data-Intensive Applications") }   // 5.0 with 2 reviews
            jsonPath("$[1].title") { value("Pride and Prejudice") }                    // 5.0 with 1 review
            jsonPath("$[0].count") { value(2) }
        }
    }
}

class Ex05SolutionTest : Ex05Spec("/solutions/ex05")

@Tag("exercise")
class Ex05ExerciseTest : Ex05Spec("/exercises/ex05")
