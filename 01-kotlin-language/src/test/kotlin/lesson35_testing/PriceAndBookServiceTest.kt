package lesson35_testing

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlin.time.TimeSource

/**
 * Real tests for lesson 35. Run:  ./gradlew :01-kotlin-language:test --tests 'lesson35_testing.*'
 */
class PriceCalculatorTest {

    @Test
    fun `empty cart costs nothing`() {
        assertEquals(0, PriceCalculator.total(emptyList()))
    }

    @Test
    fun `applies a 10 percent discount`() {
        // Arrange
        val items = listOf(Item("tea", 700))
        // Act
        val total = PriceCalculator.total(items, discountPercent = 10)
        // Assert
        assertEquals(630, total)
    }

    @Test
    fun `rejects a discount above 100`() {
        val error = assertFailsWith<IllegalArgumentException> {
            PriceCalculator.total(emptyList(), discountPercent = 150)
        }
        assertEquals("discount must be 0..100, was 150", error.message)
    }

    @Test
    fun `table of discounts`() {
        val cases = mapOf(0 to 1000L, 25 to 750L, 100 to 0L)
        for ((discount, expected) in cases) {
            assertEquals(expected, PriceCalculator.total(listOf(Item("x", 1000)), discount), "discount $discount%")
        }
    }
}

class BookServiceTest {

    private val now = Instant.parse("2026-11-22T10:00:00Z")
    private val repository = FakeBookRepository()                 // fresh fake for every test (JUnit makes a new instance per test)
    private val service = BookService(repository, FixedClock(now))

    @Test
    fun `adds a trimmed book stamped with the clock`() {
        service.addBook("978-1", "  Dune ")

        val saved = assertNotNull(repository.findByIsbn("978-1"))
        assertEquals("Dune", saved.title)
        assertEquals(now, saved.addedAt)
    }

    @Test
    fun `refuses duplicates`() {
        service.addBook("978-1", "Dune")
        assertFailsWith<IllegalStateException> { service.addBook("978-1", "Dune again") }
        assertEquals(1, repository.saved.size)
    }
}

class CoroutineTimeTest {

    private suspend fun slowGreeting(): String {
        delay(10.minutes)                     // would take 10 real minutes outside runTest
        return "hello"
    }

    @Test
    fun `virtual time skips the delay`() = runTest {
        val realStart = TimeSource.Monotonic.markNow()
        assertEquals("hello", slowGreeting())
        assertTrue(realStart.elapsedNow() < 10.minutes, "runTest should not really wait")
    }
}
