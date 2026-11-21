package exercises.ex32

import org.junit.jupiter.api.Tag
import solutions.ex32.Ex32Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex32Spec(private val impl: Ex32) {

    private val noon = Instant.parse("2026-11-22T12:00:00Z")
    private fun clockAt(instant: Instant) = object : Clock {
        override fun now() = instant
    }

    @Test
    fun `32_1 expiresAt adds the lifetime to now`() {
        assertEquals(Instant.parse("2026-11-29T12:00:00Z"), impl.expiresAt(clockAt(noon), 7.days))
    }

    @Test
    fun `32_2 isExpired compares with the clock`() {
        assertFalse(impl.isExpired(noon + 1.seconds, clockAt(noon)))
        assertTrue(impl.isExpired(noon, clockAt(noon)))
        assertTrue(impl.isExpired(noon - 1.seconds, clockAt(noon)))
    }

    @Test
    fun `32_3 toBase64Url is url safe without padding`() {
        assertEquals("aGk", impl.toBase64Url("hi".encodeToByteArray()))
        assertEquals("-__-", impl.toBase64Url(byteArrayOf(-5, -1, -2)))
    }

    @Test
    fun `32_4 sha256Hex matches the known digest`() {
        assertEquals(
            "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
            impl.sha256Hex("hello"),
        )
    }
}

class Ex32SolutionTest : Ex32Spec(Ex32Solution)

@Tag("exercise")
class Ex32ExerciseTest : Ex32Spec(Ex32Exercise)
