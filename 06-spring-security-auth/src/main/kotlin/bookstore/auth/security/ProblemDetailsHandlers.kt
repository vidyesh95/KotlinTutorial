package bookstore.auth.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

/**
 * Returns RFC 9457 Problem Details JSON for unauthenticated requests (401 Unauthorized),
 * avoiding HTML redirect pages or empty responses.
 */
@Component
class ProblemDetailsAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/problem+json;charset=UTF-8"
        val message = (authException.message ?: "Authentication is required to access this resource")
            .replace("\"", "\\\"")
        val uri = request.requestURI.replace("\"", "\\\"")
        val json = """{"type":"about:blank","title":"Unauthorized","status":401,"detail":"$message","instance":"$uri"}"""
        response.writer.write(json)
    }
}

/**
 * Returns RFC 9457 Problem Details JSON for authenticated users lacking required role (403 Forbidden).
 */
@Component
class ProblemDetailsAccessDeniedHandler : AccessDeniedHandler {

    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException,
    ) {
        response.status = HttpStatus.FORBIDDEN.value()
        response.contentType = "application/problem+json;charset=UTF-8"
        val message = (accessDeniedException.message ?: "You do not have permission to access this resource")
            .replace("\"", "\\\"")
        val uri = request.requestURI.replace("\"", "\\\"")
        val json = """{"type":"about:blank","title":"Forbidden","status":403,"detail":"$message","instance":"$uri"}"""
        response.writer.write(json)
    }
}
