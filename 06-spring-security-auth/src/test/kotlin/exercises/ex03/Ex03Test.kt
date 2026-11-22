package exercises.ex03

import bookstore.auth.domain.Role
import bookstore.auth.domain.UserPrincipal
import exercises.SpringSecurityExerciseSpec
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user
import org.springframework.test.web.servlet.get

abstract class Ex03Spec(base: String) : SpringSecurityExerciseSpec(base) {

    private fun principal(id: String, role: Role) =
        UserPrincipal(id = id, name = "Test User", email = "$id@test.com", role = role)

    @Test
    fun `03_1 admin-ping requires ADMIN role`() {
        // User with ROLE_USER gets 403 Forbidden
        mockMvc.get("$base/admin-ping") {
            with(user(principal("usr_alice", Role.USER)))
        }.andExpect {
            status { isForbidden() }
        }

        // User with ROLE_ADMIN gets 200 OK
        mockMvc.get("$base/admin-ping") {
            with(user(principal("usr_admin", Role.ADMIN)))
        }.andExpect {
            status { isOk() }
            jsonPath("$.status") { value("admin-ok") }
        }
    }

    @Test
    fun `03_2 user-data allows owner or admin`() {
        // Alice can access Alice's data
        mockMvc.get("$base/user-data/usr_alice") {
            with(user(principal("usr_alice", Role.USER)))
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value("usr_alice") }
        }

        // Alice cannot access Bob's data
        mockMvc.get("$base/user-data/usr_bob") {
            with(user(principal("usr_alice", Role.USER)))
        }.andExpect {
            status { isForbidden() }
        }

        // Admin can access Bob's data
        mockMvc.get("$base/user-data/usr_bob") {
            with(user(principal("usr_admin", Role.ADMIN)))
        }.andExpect {
            status { isOk() }
            jsonPath("$.userId") { value("usr_bob") }
        }
    }
}

class Ex03SolutionTest : Ex03Spec("/solutions/ex03")

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec("/exercises/ex03")
