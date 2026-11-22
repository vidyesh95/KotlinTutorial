package bookstore

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ResourceNotFoundException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.CONFLICT)
class ConflictException(message: String) : RuntimeException(message)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidCredentialsException(message: String = "Invalid email or password") : RuntimeException(message)

@ResponseStatus(HttpStatus.UNAUTHORIZED)
class InvalidSessionException(message: String = "Session is invalid or expired") : RuntimeException(message)
