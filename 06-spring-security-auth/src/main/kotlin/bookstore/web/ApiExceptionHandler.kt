package bookstore.web

import bookstore.ConflictException
import bookstore.InvalidCredentialsException
import bookstore.InvalidSessionException
import bookstore.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(ex: ResourceNotFoundException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.message ?: "Resource not found")
        detail.title = "Not Found"
        return detail
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(ex: ConflictException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.message ?: "Conflict detected")
        detail.title = "Conflict"
        return detail
    }

    @ExceptionHandler(InvalidCredentialsException::class)
    fun handleInvalidCredentials(ex: InvalidCredentialsException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.message ?: "Invalid credentials")
        detail.title = "Unauthorized"
        return detail
    }

    @ExceptionHandler(InvalidSessionException::class)
    fun handleInvalidSession(ex: InvalidSessionException): ProblemDetail {
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, ex.message ?: "Invalid or expired session")
        detail.title = "Unauthorized"
        return detail
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(ex: MethodArgumentNotValidException): ProblemDetail {
        val fieldErrors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        val detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, fieldErrors.joinToString("; "))
        detail.title = "Bad Request"
        detail.setProperty("errors", fieldErrors)
        return detail
    }
}
