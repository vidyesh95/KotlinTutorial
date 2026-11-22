package exercises.ex02

import exercises.SpringSecurityExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

abstract class Ex02Spec(base: String) : SpringSecurityExerciseSpec(base) {

    @Test
    fun `02_1 valid seed session returns active`() {
        val token = "seed-alice-session-token-12345"
        mockMvc.post("$base/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"$token"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.active") { value(true) }
            jsonPath("$.userId") { value("usr_alice") }
        }

        mockMvc.post("$base/validate") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"non_existent_token"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.active") { value(false) }
        }
    }

    @Test
    fun `02_2 extend updates expiration date`() {
        val token = "seed-alice-session-token-12345"
        mockMvc.post("$base/extend") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"token":"$token","extensionMinutes":60}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.success") { value(true) }
            jsonPath("$.newExpiresAt") { isNotEmpty() }
        }
    }
}

class Ex02SolutionTest : Ex02Spec("/solutions/ex02")

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec("/exercises/ex02")
