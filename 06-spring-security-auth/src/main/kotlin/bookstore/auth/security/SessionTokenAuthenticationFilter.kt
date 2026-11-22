package bookstore.auth.security

import bookstore.auth.domain.UserPrincipal
import bookstore.auth.service.AuthService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class SessionTokenAuthenticationFilter(
    private val authService: AuthService,
) : OncePerRequestFilter() {

    companion object {
        const val SESSION_TOKEN_ATTRIBUTE = "SESSION_TOKEN"
        const val COOKIE_NAME = "better-auth.session_token"
    }

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)
        if (token != null) {
            val sessionWithUser = authService.validateSession(token)
            if (sessionWithUser != null) {
                val principal = UserPrincipal(
                    id = sessionWithUser.user.id,
                    name = sessionWithUser.user.name,
                    email = sessionWithUser.user.email,
                    role = sessionWithUser.user.role,
                )
                val authentication = UsernamePasswordAuthenticationToken(
                    principal,
                    token,
                    principal.authorities,
                )
                SecurityContextHolder.getContext().authentication = authentication
                request.setAttribute(SESSION_TOKEN_ATTRIBUTE, token)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        val authHeader = request.getHeader("Authorization")
        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ", ignoreCase = true)) {
            val token = authHeader.substring(7).trim()
            if (token.isNotEmpty()) return token
        }

        val cookies = request.cookies
        if (cookies != null) {
            for (cookie in cookies) {
                if (cookie.name == COOKIE_NAME && !cookie.value.isNullOrBlank()) {
                    return cookie.value.trim()
                }
            }
        }
        return null
    }
}
