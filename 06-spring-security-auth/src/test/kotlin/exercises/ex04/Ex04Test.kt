package exercises.ex04

import bookstore.auth.domain.Role
import bookstore.auth.domain.Session
import bookstore.auth.domain.User
import bookstore.auth.repository.SessionRepository
import bookstore.auth.repository.UserRepository
import exercises.SpringSecurityExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post
import java.time.Instant

abstract class Ex04Spec(base: String) : SpringSecurityExerciseSpec(base) {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var sessionRepository: SessionRepository

    @Test
    fun `04_1 revokes other sessions leaving keepToken intact`() {
        val userId = "usr_revocation_test_${System.currentTimeMillis()}"
        val now = Instant.now()
        val keepToken = "keep_me_${System.currentTimeMillis()}"

        // Insert user first so foreign key references user(id) succeed
        userRepository.insert(User(id = userId, name = "Revoke User", email = "$userId@test.com", role = Role.USER))

        // Seed 3 sessions for this user
        sessionRepository.insert(Session("s1_${System.currentTimeMillis()}", userId, keepToken, now.plusSeconds(3600), "127.0.0.1", "curl"))
        sessionRepository.insert(Session("s2_${System.currentTimeMillis()}", userId, "drop_1_${System.currentTimeMillis()}", now.plusSeconds(3600), "127.0.0.1", "curl"))
        sessionRepository.insert(Session("s3_${System.currentTimeMillis()}", userId, "drop_2_${System.currentTimeMillis()}", now.plusSeconds(3600), "127.0.0.1", "curl"))

        mockMvc.post("$base/revoke-others") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"userId":"$userId","keepToken":"$keepToken"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.revokedCount") { value(2) }
        }

        // Verify remaining session is keepToken
        val remaining = sessionRepository.findByUserId(userId)
        assert(remaining.size == 1)
        assert(remaining.first().token == keepToken)
    }
}

class Ex04SolutionTest : Ex04Spec("/solutions/ex04")

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec("/exercises/ex04")
