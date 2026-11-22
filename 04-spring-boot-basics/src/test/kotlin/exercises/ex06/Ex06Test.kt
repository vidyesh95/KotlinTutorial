package exercises.ex06

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

/** Tests in one class share a database, so each test uses different books (order-independent). */
abstract class Ex06Spec(base: String) : SpringExerciseSpec(base) {

    private fun move(from: Long, to: Long, quantity: Int): ResultActionsDsl = mockMvc.post("$base/move-stock") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"fromBookId":$from,"toBookId":$to,"quantity":$quantity}"""
    }

    private fun expectStock(bookId: Long, stock: Int) {
        mockMvc.get("/api/books/$bookId").andExpect { jsonPath("$.stock") { value(stock) } }
    }

    @Test
    fun `06_1 moves stock between books`() {
        move(from = 3, to = 2, quantity = 5).andExpect {
            status { isOk() }
            jsonPath("$.fromStock") { value(25) }
            jsonPath("$.toStock") { value(7) }
        }
    }

    @Test
    fun `06_1 not enough stock gives 409 and changes nothing`() {
        move(from = 4, to = 5, quantity = 1).andExpect { status { isConflict() } }   // Emma has 0
        expectStock(4, 0)
        expectStock(5, 4)
    }

    @Test
    fun `06_2 unknown target rolls back the source`() {
        move(from = 1, to = 999, quantity = 3).andExpect { status { isNotFound() } }
        expectStock(1, 12)   // without @Transactional this would be 9
    }
}

class Ex06SolutionTest : Ex06Spec("/solutions/ex06")

@Tag("exercise")
class Ex06ExerciseTest : Ex06Spec("/exercises/ex06")
