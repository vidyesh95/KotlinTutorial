package bookstore.web

import bookstore.ConflictException
import bookstore.NotFoundException
import bookstore.OutOfStockException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Lesson 03 — ONE place that turns exceptions into HTTP error responses.
 *
 * Errors use the standard "Problem Details" JSON format (RFC 9457), content type
 * application/problem+json. A real response from this app:
 *   { "detail": "Book 999 not found", "status": 404, "title": "Not Found" }
 * (`type` defaults to "about:blank" and is then left out; extra fields such as
 *  `errors` or `bookId` may be added.)
 *
 * Extending ResponseEntityExceptionHandler makes Spring MVC's OWN errors (bad JSON,
 * validation failures, wrong HTTP method…) use the same format.
 *
 * 🔐 Never put stack traces or SQL in error responses — they help attackers. Log them instead.
 */
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    fun notFound(e: NotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler
    fun conflict(e: ConflictException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)

    @ExceptionHandler
    fun outOfStock(e: OutOfStockException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message).apply {
            title = "Out of stock"
            setProperty("bookId", e.bookId)          // extra machine-readable fields are allowed
        }

    /**
     * By default a failed @Valid body gives only "Invalid request content.". Add WHICH fields
     * failed and why, so API clients can show the message next to the right form field:
     *   "errors": { "title": "must not be blank", "isbn": "must be exactly 13 digits" }
     */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        val problem = ex.body.apply {
            detail = "Validation failed"
            setProperty("errors", ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage })
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }

    @ExceptionHandler
    fun badArgument(e: IllegalArgumentException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.message ?: "Bad request")
}
