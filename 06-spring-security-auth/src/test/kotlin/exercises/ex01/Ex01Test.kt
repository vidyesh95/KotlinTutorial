package exercises.ex01

import exercises.SpringSecurityExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.post

abstract class Ex01Spec(base: String) : SpringSecurityExerciseSpec(base) {

    @Test
    fun `01_1 hashing produces valid argon2id hash via DelegatingPasswordEncoder`() {
        val result = mockMvc.post("$base/hash") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"password":"supersecretpassword"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.hash") { isNotEmpty() }
        }.andReturn()

        val hash = """"hash":"([^"]+)"""".toRegex().find(result.response.contentAsString)!!.groupValues[1]
        // Must start with {argon2}$argon2id$ matching Better-Auth's default algorithm
        assert(hash.startsWith("{argon2}\$argon2id\$"))
    }

    @Test
    fun `01_2 verification correctly validates both Argon2id and BCrypt hashes`() {
        // Pre-computed Argon2id hash for password123
        val argon2Hash = "{argon2}\$argon2id\$v=19\$m=16384,t=2,p=1\$DI+Ohn0u9IBNv9Ml4bGJVQ\$TKIT970MRb8blo2FF8IkYq2hLmvdDxC04pdljhW8CLQ"
        // Pre-computed BCrypt hash for password123
        val bcryptHash = "{bcrypt}\$2a\$10\$oKeRrC.rFnePPRMo6MdqI.5SLaHDeVro4revgXaRBm8Nw2Vjz4YBG"

        // Correct password with Argon2id
        mockMvc.post("$base/verify") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"password":"password123","hash":"$argon2Hash"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.valid") { value(true) }
        }

        // Correct password with BCrypt (DelegatingPasswordEncoder verifies both!)
        mockMvc.post("$base/verify") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"password":"password123","hash":"$bcryptHash"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.valid") { value(true) }
        }

        // Wrong password
        mockMvc.post("$base/verify") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"password":"wrongpassword","hash":"$argon2Hash"}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.valid") { value(false) }
        }
    }
}

class Ex01SolutionTest : Ex01Spec("/solutions/ex01")

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec("/exercises/ex01")
