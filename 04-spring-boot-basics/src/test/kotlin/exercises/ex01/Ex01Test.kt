package exercises.ex01

import exercises.SpringExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

abstract class Ex01Spec(base: String) : SpringExerciseSpec(base) {
    @Test
    fun `01_1 hello greets by name`() {
        mockMvc.get("$base/hello?name=Ada").andExpect {
            status { isOk() }
            jsonPath("$.message") { value("Hello, Ada! Welcome to Kotlin Books") }
        }
        mockMvc.get("$base/hello").andExpect {
            jsonPath("$.message") { value("Hello, stranger! Welcome to Kotlin Books") }
        }
    }

    @Test
    fun `01_2 info shows config and book count`() {
        mockMvc.get("$base/info").andExpect {
            status { isOk() }
            jsonPath("$.app") { value("Kotlin Books") }
            jsonPath("$.currency") { value("EUR") }
            jsonPath("$.bookCount") { value(5) }
        }
    }
}

class Ex01SolutionTest : Ex01Spec("/solutions/ex01")

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec("/exercises/ex01")
