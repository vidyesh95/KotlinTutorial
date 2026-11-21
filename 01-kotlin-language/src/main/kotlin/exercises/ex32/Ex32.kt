package exercises.ex32

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/*
 * Exercise 32 — Time, ids and encoding          (lesson: lesson32_time_ids_encoding)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex32/Ex32.kt.
 */
interface Ex32 {
    /**
     * 32.1 — When does a session created now expire? Return clock.now() + [lifetime].
     * Use the [clock] parameter, NOT Clock.System — that's what makes it testable.
     */
    fun expiresAt(clock: Clock, lifetime: Duration): Instant

    /** 32.2 — True when [expiresAt] is at or before the clock's now. */
    fun isExpired(expiresAt: Instant, clock: Clock): Boolean

    /** 32.3 — URL-safe Base64 WITHOUT padding. bytes of "hi" → "aGk" */
    fun toBase64Url(bytes: ByteArray): String

    /** 32.4 — SHA-256 of the UTF-8 bytes of [text], as lowercase hex (64 chars). */
    fun sha256Hex(text: String): String
}

object Ex32Exercise : Ex32 {
    override fun expiresAt(clock: Clock, lifetime: Duration): Instant = TODO("32.1")
    override fun isExpired(expiresAt: Instant, clock: Clock): Boolean = TODO("32.2")
    override fun toBase64Url(bytes: ByteArray): String = TODO("32.3")
    override fun sha256Hex(text: String): String = TODO("32.4")
}
