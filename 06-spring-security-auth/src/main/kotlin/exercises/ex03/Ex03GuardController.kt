package exercises.ex03

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Ex03StatusResponse(val status: String)
data class Ex03UserDataResponse(val userId: String, val secret: String)

@RestController
@RequestMapping("/exercises/ex03")
class Ex03GuardController {

    /**
     * 03.1 — Restrict this endpoint so ONLY users with role 'ADMIN' can call it.
     * Hint: Use @PreAuthorize("hasRole('ADMIN')")
     * Return Ex03StatusResponse("admin-ok").
     */
    @GetMapping("/admin-ping")
    fun adminPing(): Ex03StatusResponse = TODO("03.1")

    /**
     * 03.2 — Restrict this endpoint so users can only access their OWN data (where principal.id == userId),
     * OR any user with role 'ADMIN' can access it.
     * Hint: Use @PreAuthorize("hasRole('ADMIN') or (principal instanceof T(bookstore.auth.domain.UserPrincipal) and principal.id == #userId)")
     * Return Ex03UserDataResponse(userId = userId, secret = "secret-for-$userId").
     */
    @GetMapping("/user-data/{userId}")
    fun getUserData(@PathVariable userId: String): Ex03UserDataResponse = TODO("03.2")
}
