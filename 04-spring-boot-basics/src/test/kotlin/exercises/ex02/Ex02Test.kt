package exercises.ex02

import exercises.SpringExerciseSpec
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.matchesPattern
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

abstract class Ex02Spec(base: String) : SpringExerciseSpec(base) {
    @Test
    fun `02_1 lists authors ordered by id`() {
        mockMvc.get("$base/authors").andExpect {
            status { isOk() }
            jsonPath("$.length()") { value(greaterThanOrEqualTo(3)) }   // ≥: another test may have added one
            jsonPath("$[0].name") { value("Frank Herbert") }
            jsonPath("$[2].name") { value("Martin Kleppmann") }
        }
    }

    @Test
    fun `02_2 get one author or 404`() {
        mockMvc.get("$base/authors/2").andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Jane Austen") }
        }
        mockMvc.get("$base/authors/99").andExpect {
            status { isNotFound() }
            jsonPath("$.detail") { value("Author 99 not found") }
        }
    }

    @Test
    fun `02_3 create returns 201 with Location`() {
        val location = mockMvc.post("$base/authors") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Ursula K. Le Guin"}"""
        }.andExpect {
            status { isCreated() }
            header { string("Location", matchesPattern(".*$base/authors/\\d+")) }
            jsonPath("$.name") { value("Ursula K. Le Guin") }
        }.andReturn().response.getHeader("Location")!!

        mockMvc.get(location).andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Ursula K. Le Guin") }
        }
    }
}

class Ex02SolutionTest : Ex02Spec("/solutions/ex02")

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec("/exercises/ex02")
