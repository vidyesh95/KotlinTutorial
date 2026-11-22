package solutions.ex02

import bookstore.auth.repository.SessionRepository
import exercises.ex02.Ex02ExtendRequest
import exercises.ex02.Ex02ExtendResponse
import exercises.ex02.Ex02ValidateRequest
import exercises.ex02.Ex02ValidateResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant

@RestController
@RequestMapping("/solutions/ex02")
class Ex02SessionSolutionController(
    private val sessionRepository: SessionRepository,
) {
    @PostMapping("/validate")
    fun validateSession(@RequestBody request: Ex02ValidateRequest): Ex02ValidateResponse {
        val session = sessionRepository.findByToken(request.token)
        if (session == null || session.isExpired()) {
            return Ex02ValidateResponse(active = false, userId = null)
        }
        return Ex02ValidateResponse(active = true, userId = session.userId)
    }

    @PostMapping("/extend")
    fun extendSession(@RequestBody request: Ex02ExtendRequest): Ex02ExtendResponse {
        val session = sessionRepository.findByToken(request.token)
        if (session == null || session.isExpired()) {
            return Ex02ExtendResponse(success = false, newExpiresAt = null)
        }
        val newExpiresAt = session.expiresAt.plus(Duration.ofMinutes(request.extensionMinutes))
        sessionRepository.updateExpiresAt(request.token, newExpiresAt)
        return Ex02ExtendResponse(success = true, newExpiresAt = newExpiresAt)
    }
}
