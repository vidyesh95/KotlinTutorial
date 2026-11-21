package exercises.ex35

import org.junit.jupiter.api.Tag
import solutions.ex35.Ex35Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex35Spec(private val impl: Ex35) {

    @Test
    fun `35_1 fake repository stores, finds and replaces by email`() {
        val repo = impl.newFakeUserRepository()
        assertEquals(0, repo.count())
        repo.save(User("ada@example.com", "Ada"))
        repo.save(User("bob@example.com", "Bob"))
        repo.save(User("ada@example.com", "Ada Lovelace"))
        assertEquals(2, repo.count())
        assertEquals("Ada Lovelace", repo.findByEmail("ada@example.com")?.name)
        assertNull(repo.findByEmail("nobody@example.com"))
    }

    @Test
    fun `35_1 every fake starts empty`() {
        impl.newFakeUserRepository().save(User("a@b.cd", "A"))
        assertEquals(0, impl.newFakeUserRepository().count())
    }

    @Test
    fun `35_2 recording sender remembers sends in order`() {
        val sender = impl.newRecordingEmailSender()
        sender.send("ada@example.com", "Welcome")
        sender.send("bob@example.com", "Reset password")
        assertEquals(listOf("ada@example.com|Welcome", "bob@example.com|Reset password"), sender.sent)
    }

    @Test
    fun `35_3 failingCases reports wrong answers`() {
        assertEquals(listOf(3), impl.failingCases(mapOf(2 to 4, 3 to 10)) { it * 2 })
        assertEquals(emptyList(), impl.failingCases(mapOf("a" to "A")) { it.uppercase() })
    }
}

class Ex35SolutionTest : Ex35Spec(Ex35Solution)

@Tag("exercise")
class Ex35ExerciseTest : Ex35Spec(Ex35Exercise)
