package bookstore.auth.domain

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

enum class Role {
    USER,
    ADMIN;

    fun toAuthority(): String = "ROLE_$name"
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val emailVerified: Boolean = false,
    val image: String? = null,
    val role: Role = Role.USER,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class Account(
    val id: String,
    val userId: String,
    val accountId: String,
    val providerId: String = "credential",
    val passwordHash: String?,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class Session(
    val id: String,
    val userId: String,
    val token: String,
    val expiresAt: Instant,
    val ipAddress: String? = null,
    val userAgent: String? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
) {
    fun isExpired(now: Instant = Instant.now()): Boolean = now.isAfter(expiresAt)
}

data class Verification(
    val id: String,
    val identifier: String,
    val value: String,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

data class UserPrincipal(
    val id: String,
    val name: String,
    val email: String,
    val role: Role,
) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority> =
        listOf(SimpleGrantedAuthority(role.toAuthority()))

    override fun getPassword(): String? = null
    override fun getUsername(): String = email
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isEnabled(): Boolean = true
}

data class SessionWithUser(
    val session: Session,
    val user: User,
)
