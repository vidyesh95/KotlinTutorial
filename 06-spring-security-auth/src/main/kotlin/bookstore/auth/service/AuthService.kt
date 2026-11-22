package bookstore.auth.service

import bookstore.ConflictException
import bookstore.InvalidCredentialsException
import bookstore.auth.domain.*
import bookstore.auth.repository.AccountRepository
import bookstore.auth.repository.SessionRepository
import bookstore.auth.repository.UserRepository
import bookstore.auth.web.AuthResponse
import bookstore.auth.web.SignInRequest
import bookstore.auth.web.SignUpRequest
import bookstore.auth.web.UserDto
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val accountRepository: AccountRepository,
    private val sessionRepository: SessionRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    private val secureRandom = SecureRandom()
    private val sessionDuration = Duration.ofDays(7)

    @Transactional
    fun signUp(request: SignUpRequest, ipAddress: String? = null, userAgent: String? = null): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        if (userRepository.findByEmail(normalizedEmail) != null) {
            throw ConflictException("User with email '$normalizedEmail' already exists")
        }

        val userId = "usr_${UUID.randomUUID()}"
        val now = Instant.now()
        val user = User(
            id = userId,
            name = request.name.trim(),
            email = normalizedEmail,
            emailVerified = false,
            role = Role.USER,
            createdAt = now,
            updatedAt = now,
        )
        userRepository.insert(user)

        val accountId = "acc_${UUID.randomUUID()}"
        val account = Account(
            id = accountId,
            userId = userId,
            accountId = normalizedEmail,
            providerId = "credential",
            passwordHash = passwordEncoder.encode(request.password),
            createdAt = now,
            updatedAt = now,
        )
        accountRepository.insert(account)

        val session = createSession(userId, ipAddress, userAgent)
        return AuthResponse(
            user = UserDto.from(user),
            token = session.token,
            expiresAt = session.expiresAt,
        )
    }

    @Transactional
    fun signIn(request: SignInRequest, ipAddress: String? = null, userAgent: String? = null): AuthResponse {
        val normalizedEmail = request.email.trim().lowercase()
        val user = userRepository.findByEmail(normalizedEmail)
            ?: throw InvalidCredentialsException()

        val account = accountRepository.findByUserIdAndProvider(user.id, "credential")
            ?: throw InvalidCredentialsException()

        val hash = account.passwordHash
        if (hash == null || !passwordEncoder.matches(request.password, hash)) {
            throw InvalidCredentialsException()
        }

        val session = createSession(user.id, ipAddress, userAgent)
        return AuthResponse(
            user = UserDto.from(user),
            token = session.token,
            expiresAt = session.expiresAt,
        )
    }

    @Transactional
    fun signOut(token: String) {
        sessionRepository.deleteByToken(token)
    }

    @Transactional
    fun revokeAllOtherSessions(userId: String, currentToken: String) {
        sessionRepository.deleteByUserIdExcept(userId, currentToken)
    }

    @Transactional
    fun revokeAllSessions(userId: String) {
        sessionRepository.deleteByUserId(userId)
    }

    fun validateSession(token: String): SessionWithUser? {
        val session = sessionRepository.findByToken(token) ?: return null
        val now = Instant.now()
        if (session.isExpired(now)) {
            sessionRepository.deleteByToken(token)
            return null
        }
        val user = userRepository.findById(session.userId) ?: return null
        return SessionWithUser(session, user)
    }

    fun listUserSessions(userId: String): List<Session> =
        sessionRepository.findByUserId(userId)

    private fun createSession(userId: String, ipAddress: String?, userAgent: String?): Session {
        val now = Instant.now()
        val tokenBytes = ByteArray(32)
        secureRandom.nextBytes(tokenBytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)

        val session = Session(
            id = "ses_${UUID.randomUUID()}",
            userId = userId,
            token = token,
            expiresAt = now.plus(sessionDuration),
            ipAddress = ipAddress,
            userAgent = userAgent,
            createdAt = now,
            updatedAt = now,
        )
        return sessionRepository.insert(session)
    }
}
