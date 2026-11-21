package exercises.ex02

import org.junit.jupiter.api.Tag
import solutions.ex02.Ex02Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

abstract class Ex02Spec(private val impl: Ex02) {
    private val morning = Instant.parse("2026-05-01T08:15:00Z")
    private val afternoon = Instant.parse("2026-05-01T15:45:00Z")

    @Test
    fun `02_1 recording mailer remembers mails in order`() {
        val mailer = impl.newRecordingMailer()
        mailer.mail("ada", "hi")
        mailer.mail("bob", "yo")
        assertEquals(listOf("ada: hi", "bob: yo"), mailer.mails)
    }

    @Test
    fun `02_2 fixed time never moves`() {
        val time = impl.fixedTime(morning)
        assertEquals(morning, time.now())
        assertEquals(morning, time.now())
    }

    @Test
    fun `02_3 greeter depends on injected time`() {
        val mailer = impl.newRecordingMailer()
        impl.newGreeter(impl.fixedTime(morning), mailer).greet("ada")
        impl.newGreeter(impl.fixedTime(afternoon), mailer).greet("bob")
        assertEquals(listOf("ada: Good morning, ada", "bob: Good afternoon, bob"), mailer.mails)
    }
}

class Ex02SolutionTest : Ex02Spec(Ex02Solution)

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec(Ex02Exercise)
