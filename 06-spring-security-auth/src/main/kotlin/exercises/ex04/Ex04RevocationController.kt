package exercises.ex04

import bookstore.auth.repository.SessionRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Ex04RevokeRequest(val userId: String, val keepToken: String)
data class Ex04RevokeResponse(val revokedCount: Int)

@RestController
@RequestMapping("/exercises/ex04")
class Ex04RevocationController(
    private val sessionRepository: SessionRepository,
) {
    /**
     * 04.1 — Revoke all other active sessions for this user except the one designated by keepToken.
     * Call sessionRepository.deleteByUserIdExcept(request.userId, request.keepToken)
     * Return Ex04RevokeResponse with the number of sessions deleted.
     */
    @PostMapping("/revoke-others")
    fun revokeOtherSessions(@RequestBody request: Ex04RevokeRequest): Ex04RevokeResponse = TODO("04.1")
}
