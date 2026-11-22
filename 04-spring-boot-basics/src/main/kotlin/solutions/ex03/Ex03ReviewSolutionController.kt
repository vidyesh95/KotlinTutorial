package solutions.ex03

import bookstore.NotFoundException
import bookstore.books.BookRepository
import exercises.ex03.ReviewAccepted
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.Size
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice

data class ValidatedReviewRequest(
    @field:Positive val bookId: Long,
    @field:Min(1) @field:Max(5) val rating: Int,
    @field:NotBlank @field:Email val email: String,
    @field:Size(max = 500) val text: String? = null,
)

class SolutionSpamReviewException : RuntimeException("Review looks like spam")

@RestController
@RequestMapping("/solutions/ex03")
class Ex03ReviewSolutionController(private val books: BookRepository) {

    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun submit(@Valid @RequestBody request: ValidatedReviewRequest): ReviewAccepted {
        books.findById(request.bookId) ?: throw NotFoundException("Book", request.bookId)
        if (request.text?.contains("spam", ignoreCase = true) == true) throw SolutionSpamReviewException()
        return ReviewAccepted(request.bookId, request.rating, accepted = true)
    }
}

@RestControllerAdvice(assignableTypes = [Ex03ReviewSolutionController::class])
class Ex03SolutionExceptionHandler {
    @ExceptionHandler
    fun spam(e: SolutionSpamReviewException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, e.message).apply {
            title = "Spam detected"
        }
}
