package exercises.ex35

/*
 * Exercise 35 — Testing: write your own test doubles          (lesson: lesson35_testing)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex35/Ex35.kt.
 */

data class User(val email: String, val name: String)

interface UserRepository {
    fun save(user: User)
    fun findByEmail(email: String): User?
    fun count(): Int
}

interface EmailSender {
    fun send(to: String, subject: String)
}

/** An [EmailSender] for tests: instead of sending, it remembers what would have been sent. */
interface RecordingEmailSender : EmailSender {
    /** Every send call so far, as "to|subject", in order. */
    val sent: List<String>
}

interface Ex35 {
    /** 35.1 — Return a NEW, empty in-memory [UserRepository] (a fake). Emails are unique keys: saving again replaces. */
    fun newFakeUserRepository(): UserRepository

    /** 35.2 — Return a NEW [RecordingEmailSender]. */
    fun newRecordingEmailSender(): RecordingEmailSender

    /**
     * 35.3 — A tiny table-driven test runner: return the inputs (in order) for which
     * `function(input) != expected`. failingCases(mapOf(2 to 4, 3 to 10)) { it * 2 } → [3]
     */
    fun <I, O> failingCases(cases: Map<I, O>, function: (I) -> O): List<I>
}

object Ex35Exercise : Ex35 {
    override fun newFakeUserRepository(): UserRepository = TODO("35.1")
    override fun newRecordingEmailSender(): RecordingEmailSender = TODO("35.2")
    override fun <I, O> failingCases(cases: Map<I, O>, function: (I) -> O): List<I> = TODO("35.3")
}
