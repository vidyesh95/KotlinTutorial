package exercises.ex02

import kotlin.time.Instant

/*
 * Exercise 02 — Interfaces, fakes and time         (lesson: lesson02_constructor_injection)
 * Replace every TODO(...) then run: ./gradlew :02-dependency-injection:exercises
 */

fun interface TimeSource {
    fun now(): Instant
}

interface Mailer {
    fun mail(to: String, body: String)
}

/** A fake Mailer that remembers every mail as "<to>: <body>". */
interface RecordingMailer : Mailer {
    val mails: List<String>
}

interface Greeter {
    fun greet(user: String)
}

interface Ex02 {
    /** 02.1 — A fake Mailer that records "<to>: <body>" strings, in order. */
    fun newRecordingMailer(): RecordingMailer

    /** 02.2 — A TimeSource that always returns [instant]. */
    fun fixedTime(instant: Instant): TimeSource

    /**
     * 02.3 — A Greeter that mails [user] "Good morning, <user>" when the UTC hour of time.now()
     * is before 12, otherwise "Good afternoon, <user>". Use ONLY the injected time and mailer.
     * Hint: UTC hour = (now.epochSeconds / 3600) % 24
     */
    fun newGreeter(time: TimeSource, mailer: Mailer): Greeter
}

object Ex02Exercise : Ex02 {
    override fun newRecordingMailer(): RecordingMailer = TODO("02.1")
    override fun fixedTime(instant: Instant): TimeSource = TODO("02.2")
    override fun newGreeter(time: TimeSource, mailer: Mailer): Greeter = TODO("02.3")
}
