package bookstore.auth.web

import bookstore.auth.domain.UserPrincipal
import bookstore.auth.security.SessionTokenAuthenticationFilter
import bookstore.auth.service.AuthService
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.time.Duration

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService,
) {

    @PostMapping("/sign-up")
    fun signUp(
        @Valid @RequestBody request: SignUpRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<AuthResponse> {
        val auth = authService.signUp(
            request = request,
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent"),
        )
        val cookie = createSessionCookie(auth.token, Duration.ofDays(7))
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(auth)
    }

    @PostMapping("/sign-in")
    fun signIn(
        @Valid @RequestBody request: SignInRequest,
        httpRequest: HttpServletRequest,
    ): ResponseEntity<AuthResponse> {
        val auth = authService.signIn(
            request = request,
            ipAddress = httpRequest.remoteAddr,
            userAgent = httpRequest.getHeader("User-Agent"),
        )
        val cookie = createSessionCookie(auth.token, Duration.ofDays(7))
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, cookie.toString())
            .body(auth)
    }

    @PostMapping("/sign-out")
    fun signOut(
        httpRequest: HttpServletRequest,
        @RequestHeader(value = "Authorization", required = false) authHeader: String?,
    ): ResponseEntity<MessageResponse> {
        val token = httpRequest.getAttribute(SessionTokenAuthenticationFilter.SESSION_TOKEN_ATTRIBUTE) as? String
            ?: authHeader?.removePrefix("Bearer ")?.trim()

        if (token != null) {
            authService.signOut(token)
        }

        val clearCookie = createSessionCookie("", Duration.ZERO)
        return ResponseEntity
            .ok()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .body(MessageResponse("Signed out successfully"))
    }

    @GetMapping("/session", "/me")
    fun currentSession(
        @AuthenticationPrincipal principal: UserPrincipal,
        httpRequest: HttpServletRequest,
    ): CurrentSessionResponse {
        val token = httpRequest.getAttribute(SessionTokenAuthenticationFilter.SESSION_TOKEN_ATTRIBUTE) as? String
        val sessionWithUser = token?.let { authService.validateSession(it) }

        val sessionDto = if (sessionWithUser != null) {
            SessionDto.from(sessionWithUser.session, token)
        } else {
            SessionDto(
                id = "mock_session",
                token = token ?: "n/a",
                expiresAt = java.time.Instant.now().plus(Duration.ofDays(1)),
                ipAddress = httpRequest.remoteAddr,
                userAgent = httpRequest.getHeader("User-Agent"),
                createdAt = java.time.Instant.now(),
                isCurrent = true,
            )
        }

        val userDto = UserDto(
            id = principal.id,
            name = principal.name,
            email = principal.email,
            emailVerified = true,
            role = principal.role.name,
        )

        return CurrentSessionResponse(user = userDto, session = sessionDto)
    }

    @GetMapping("/sessions")
    fun listSessions(
        @AuthenticationPrincipal principal: UserPrincipal,
        httpRequest: HttpServletRequest,
    ): List<SessionDto> {
        val currentToken = httpRequest.getAttribute(SessionTokenAuthenticationFilter.SESSION_TOKEN_ATTRIBUTE) as? String
        return authService.listUserSessions(principal.id).map { SessionDto.from(it, currentToken) }
    }

    @PostMapping("/revoke-sessions")
    fun revokeAllOtherSessions(
        @AuthenticationPrincipal principal: UserPrincipal,
        httpRequest: HttpServletRequest,
    ): MessageResponse {
        val currentToken = httpRequest.getAttribute(SessionTokenAuthenticationFilter.SESSION_TOKEN_ATTRIBUTE) as? String
        if (currentToken != null) {
            authService.revokeAllOtherSessions(principal.id, currentToken)
        }
        return MessageResponse("All other active sessions have been revoked")
    }

    private fun createSessionCookie(token: String, maxAge: Duration): ResponseCookie =
        ResponseCookie.from(SessionTokenAuthenticationFilter.COOKIE_NAME, token)
            .httpOnly(true)
            .secure(false) // local tutorial environment
            .path("/")
            .maxAge(maxAge)
            .sameSite("Lax")
            .build()
}
