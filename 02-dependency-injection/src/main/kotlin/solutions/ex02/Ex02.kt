package solutions.ex02

import exercises.ex02.Ex02
import exercises.ex02.Greeter
import exercises.ex02.Mailer
import exercises.ex02.RecordingMailer
import exercises.ex02.TimeSource
import kotlin.time.Instant

class InMemoryMailer : RecordingMailer {
    override val mails = mutableListOf<String>()
    override fun mail(to: String, body: String) {
        mails += "$to: $body"
    }
}

class TimeAwareGreeter(private val time: TimeSource, private val mailer: Mailer) : Greeter {
    override fun greet(user: String) {
        val hour = (time.now().epochSeconds / 3600) % 24
        val greeting = if (hour < 12) "Good morning" else "Good afternoon"
        mailer.mail(user, "$greeting, $user")
    }
}

object Ex02Solution : Ex02 {
    override fun newRecordingMailer(): RecordingMailer = InMemoryMailer()
    override fun fixedTime(instant: Instant): TimeSource = TimeSource { instant }
    override fun newGreeter(time: TimeSource, mailer: Mailer): Greeter = TimeAwareGreeter(time, mailer)
}
