package solutions.ex35

import exercises.ex35.Ex35
import exercises.ex35.RecordingEmailSender
import exercises.ex35.User
import exercises.ex35.UserRepository

object Ex35Solution : Ex35 {
    override fun newFakeUserRepository(): UserRepository = InMemoryUserRepository()

    override fun newRecordingEmailSender(): RecordingEmailSender = ListEmailSender()

    override fun <I, O> failingCases(cases: Map<I, O>, function: (I) -> O): List<I> =
        cases.filter { (input, expected) -> function(input) != expected }.keys.toList()
}

private class InMemoryUserRepository : UserRepository {
    private val users = linkedMapOf<String, User>()
    override fun save(user: User) { users[user.email] = user }
    override fun findByEmail(email: String): User? = users[email]
    override fun count(): Int = users.size
}

private class ListEmailSender : RecordingEmailSender {
    override val sent = mutableListOf<String>()
    override fun send(to: String, subject: String) { sent += "$to|$subject" }
}
