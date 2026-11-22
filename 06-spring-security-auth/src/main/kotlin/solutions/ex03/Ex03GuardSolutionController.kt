package solutions.ex03

import exercises.ex03.Ex03StatusResponse
import exercises.ex03.Ex03UserDataResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/solutions/ex03")
class Ex03GuardSolutionController {

    @GetMapping("/admin-ping")
    @PreAuthorize("hasRole('ADMIN')")
    fun adminPing(): Ex03StatusResponse =
        Ex03StatusResponse("admin-ok")

    @GetMapping("/user-data/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (principal instanceof T(bookstore.auth.domain.UserPrincipal) and principal.id == #userId)")
    fun getUserData(@PathVariable userId: String): Ex03UserDataResponse =
        Ex03UserDataResponse(userId = userId, secret = "secret-for-$userId")
}
