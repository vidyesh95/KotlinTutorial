package exercises.ex02

import bookstore.auth.repository.SessionRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

data class Ex02ValidateRequest(val token: String)
data class Ex02ValidateResponse(val active: Boolean, val userId: String?)

data class Ex02ExtendRequest(val token: String, val extensionMinutes: Long)
data class Ex02ExtendResponse(val success: Boolean, val newExpiresAt: Instant?)

@RestController
@RequestMapping("/exercises/ex02")
class Ex02SessionController(
    private val sessionRepository: SessionRepository,
) {
    /**
     * 02.1 — Look up the session in sessionRepository by token.
     * Check if it exists and is NOT expired (session.isExpired()).
     * Return Ex02ValidateResponse(active = true, userId = session.userId) if active,
     * or Ex02ValidateResponse(active = false, userId = null) if missing or expired.
     */
    @PostMapping("/validate")
    fun validateSession(@RequestBody request: Ex02ValidateRequest): Ex02ValidateResponse = TODO("02.1")

    /**
     * 02.2 — Sliding session window: extend an active session's expiry.
     * If the session exists and is active, compute newExpiresAt = session.expiresAt.plusSeconds(extensionMinutes * 60).
     * Call sessionRepository.updateExpiresAt(request.token, newExpiresAt).
     * Return Ex02ExtendResponse(success = true, newExpiresAt = newExpiresAt).
     * If missing or expired, return Ex02ExtendResponse(success = false, newExpiresAt = null).
     */
    @PostMapping("/extend")
    fun extendSession(@RequestBody request: Ex02ExtendRequest): Ex02ExtendResponse = TODO("02.2")
}
