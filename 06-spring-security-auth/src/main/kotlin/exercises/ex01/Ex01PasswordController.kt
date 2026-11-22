package exercises.ex01

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

data class Ex01HashRequest(val password: String)
data class Ex01HashResponse(val hash: String)

data class Ex01VerifyRequest(val password: String, val hash: String)
data class Ex01VerifyResponse(val valid: Boolean)

@RestController
@RequestMapping("/exercises/ex01")
class Ex01PasswordController(
    private val passwordEncoder: PasswordEncoder,
) {
    /**
     * 01.1 — Hash the incoming raw password using the injected PasswordEncoder.
     * The encoder is Spring's DelegatingPasswordEncoder with Argon2id as the default (matching Better-Auth).
     * Return Ex01HashResponse with the resulting {argon2}$argon2id$... string.
     */
    @PostMapping("/hash")
    fun hashPassword(@RequestBody request: Ex01HashRequest): Ex01HashResponse = TODO("01.1")

    /**
     * 01.2 — Verify whether the incoming raw password matches the provided hash
     * (supporting both {argon2} and {bcrypt} prefixes via DelegatingPasswordEncoder).
     * Return Ex01VerifyResponse(valid = true/false).
     */
    @PostMapping("/verify")
    fun verifyPassword(@RequestBody request: Ex01VerifyRequest): Ex01VerifyResponse = TODO("01.2")
}
