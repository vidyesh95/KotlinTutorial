package bookstore.auth.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.DelegatingPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
class PasswordConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        val bcrypt = BCryptPasswordEncoder(10)

        val encoders: MutableMap<String, PasswordEncoder> = mutableMapOf(
            "argon2" to argon2,
            "bcrypt" to bcrypt,
        )

        // Argon2id is the default algorithm used for new hashes (matching Better-Auth)
        val delegating = DelegatingPasswordEncoder("argon2", encoders)
        // If a stored hash has no prefix, fall back to argon2 (or bcrypt)
        delegating.setDefaultPasswordEncoderForMatches(argon2)

        return delegating
    }
}
