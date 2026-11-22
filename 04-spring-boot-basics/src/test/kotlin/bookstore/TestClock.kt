package bookstore

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import kotlin.time.Clock
import kotlin.time.Instant

/** Replaces the real clock in tests (module 02: time is a dependency!). @Primary wins over Clock.System. */
@TestConfiguration(proxyBeanMethods = false)
class TestClock {
    @Bean
    @Primary
    fun fixedClock(): Clock = object : Clock {
        override fun now(): Instant = NOW
    }

    companion object {
        val NOW: Instant = Instant.parse("2026-06-01T12:00:00Z")
    }
}
