package bookstore.auth.web

import bookstore.auth.domain.Session
import bookstore.auth.domain.User
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.Instant

data class SignUpRequest(
    @field:NotBlank(message = "Name cannot be blank")
    val name: String,

    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    @field:Size(min = 8, message = "Password must be at least 8 characters")
    val password: String,
)

data class SignInRequest(
    @field:NotBlank(message = "Email cannot be blank")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password cannot be blank")
    val password: String,
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean,
    val role: String,
) {
    companion object {
        fun from(user: User): UserDto = UserDto(
            id = user.id,
            name = user.name,
            email = user.email,
            emailVerified = user.emailVerified,
            role = user.role.name,
        )
    }
}

data class AuthResponse(
    val user: UserDto,
    val token: String,
    val expiresAt: Instant,
)

data class SessionDto(
    val id: String,
    val token: String,
    val expiresAt: Instant,
    val ipAddress: String?,
    val userAgent: String?,
    val createdAt: Instant,
    val isCurrent: Boolean,
) {
    companion object {
        fun from(session: Session, currentToken: String?): SessionDto = SessionDto(
            id = session.id,
            token = session.token,
            expiresAt = session.expiresAt,
            ipAddress = session.ipAddress,
            userAgent = session.userAgent,
            createdAt = session.createdAt,
            isCurrent = session.token == currentToken,
        )
    }
}

data class CurrentSessionResponse(
    val user: UserDto,
    val session: SessionDto,
)

data class MessageResponse(
    val message: String,
)
