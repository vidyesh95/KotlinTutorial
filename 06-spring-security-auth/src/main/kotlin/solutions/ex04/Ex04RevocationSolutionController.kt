package solutions.ex04

import bookstore.auth.repository.SessionRepository
import exercises.ex04.Ex04RevokeRequest
import exercises.ex04.Ex04RevokeResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/solutions/ex04")
class Ex04RevocationSolutionController(
    private val sessionRepository: SessionRepository,
) {
    @PostMapping("/revoke-others")
    fun revokeOtherSessions(@RequestBody request: Ex04RevokeRequest): Ex04RevokeResponse {
        val count = sessionRepository.deleteByUserIdExcept(request.userId, request.keepToken)
        return Ex04RevokeResponse(revokedCount = count)
    }
}
