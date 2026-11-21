package solutions.ex32

import exercises.ex32.Ex32
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

object Ex32Solution : Ex32 {
    override fun expiresAt(clock: Clock, lifetime: Duration): Instant = clock.now() + lifetime

    override fun isExpired(expiresAt: Instant, clock: Clock): Boolean = clock.now() >= expiresAt

    override fun toBase64Url(bytes: ByteArray): String =
        Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(bytes)

    override fun sha256Hex(text: String): String =
        MessageDigest.getInstance("SHA-256").digest(text.encodeToByteArray()).toHexString()
}
