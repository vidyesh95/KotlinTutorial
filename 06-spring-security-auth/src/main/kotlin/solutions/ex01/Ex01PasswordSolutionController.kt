package solutions.ex01

import exercises.ex01.Ex01HashRequest
import exercises.ex01.Ex01HashResponse
import exercises.ex01.Ex01VerifyRequest
import exercises.ex01.Ex01VerifyResponse
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/solutions/ex01")
class Ex01PasswordSolutionController(
    private val passwordEncoder: PasswordEncoder,
) {
    @PostMapping("/hash")
    fun hashPassword(@RequestBody request: Ex01HashRequest): Ex01HashResponse =
        Ex01HashResponse(hash = passwordEncoder.encode(request.password)!!)

    @PostMapping("/verify")
    fun verifyPassword(@RequestBody request: Ex01VerifyRequest): Ex01VerifyResponse =
        Ex01VerifyResponse(valid = passwordEncoder.matches(request.password, request.hash))
}
