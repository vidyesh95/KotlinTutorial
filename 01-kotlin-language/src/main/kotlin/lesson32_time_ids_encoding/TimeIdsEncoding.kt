/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  Lesson 32 — Time, IDs and encoding: the backend toolbox
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : work with durations, instants and clocks; generate and parse UUIDs;
 *         encode bytes as Base64 / hex; build a secure session token.
 *  Needs: lessons 03–04 (numbers, strings), 16 (interfaces)
 *  Time : ~35 min
 *  Next : lesson 33 — regular expressions
 *  Exercises: exercises/ex32/Ex32.kt
 *
 *  🆕 Kotlin 2.x: everything here is in the STANDARD library now —
 *  kotlin.time.Instant/Clock (stable since 2.3), kotlin.uuid.Uuid (stable since 2.4),
 *  kotlin.io.encoding.Base64 and HexFormat (stable since 2.2).
 */
package lesson32_time_ids_encoding

import java.security.MessageDigest
import java.security.SecureRandom
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant
import kotlin.time.TimeSource
import kotlin.time.measureTime
import kotlin.time.toJavaInstant
import kotlin.uuid.Uuid

fun main() {
    section1Durations()
    section2Instants()
    section3Clocks()
    section4TimeZones()
    section5MeasuringTime()
    section6Uuids()
    section7Base64()
    section8Hex()
    section9SessionTokens()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Duration — "how long"
// ───────────────────────────────────────────────────────────────────────────
// Never pass timeouts around as a bare `Long` — is 30 seconds or milliseconds?
// `Duration` carries its unit. Create one with extension properties: 5.seconds.

fun section1Durations() {
    println("§1")
    val meeting = 1.hours + 30.minutes
    println(meeting)                              // human-readable
    println(meeting.inWholeMinutes)
    println(meeting.toIsoString())                // ISO-8601, good for JSON/config
    println(Duration.parse("PT2H15M"))
    println(1500.milliseconds)
    println(90.seconds > 1.minutes)
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Instant — "when exactly"
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    An Instant is one dot on ONE big timeline shared by the whole planet. It
//    doesn't know about time zones — "10:15 in Mumbai" and "04:45 in London"
//    can be the SAME dot. Time zones only matter when you SHOW the dot to a human.
//
// 🏗️ Store Instants in UTC in the database (`created_at`, `expires_at`),
// convert to a time zone only in the UI. Modules 04–06 do exactly this.

val launchTime: Instant = Instant.parse("2026-11-22T10:15:30Z")   // Z = UTC

fun section2Instants() {
    println("§2")
    println(launchTime)
    println(launchTime + 2.days)
    println(launchTime.epochSeconds)              // seconds since 1970-01-01T00:00:00Z
    println(Instant.fromEpochSeconds(0))
    val later = Instant.parse("2026-11-22T12:00:00Z")
    println(later - launchTime)                   // Instant − Instant = Duration
    println(later > launchTime)
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Clock — make "now" replaceable
// ───────────────────────────────────────────────────────────────────────────
// Code that calls `Clock.System.now()` directly is hard to test: "now" changes
// every run. Instead ASK for a Clock. Production passes Clock.System; tests
// pass a fake clock frozen at a known moment. (This is dependency injection —
// module 02 — in its smallest form.)

class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}

fun isExpired(expiresAt: Instant, clock: Clock): Boolean = clock.now() >= expiresAt

fun section3Clocks() {
    println("§3")
    val sessionExpiresAt = launchTime + 30.minutes
    println(isExpired(sessionExpiresAt, FixedClock(launchTime + 10.minutes)))
    println(isExpired(sessionExpiresAt, FixedClock(launchTime + 31.minutes)))
    println("real clock is after 2026-01-01: ${Clock.System.now() > Instant.parse("2026-01-01T00:00:00Z")}")
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Showing an Instant in a time zone (via java.time)
// ───────────────────────────────────────────────────────────────────────────
// The Kotlin stdlib has no time zones. On the JVM convert to java.time:
// `toJavaInstant()`, then `atZone(...)` and a DateTimeFormatter.

fun section4TimeZones() {
    println("§4")
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH)
    for (zone in listOf("Asia/Kolkata", "Europe/London", "America/New_York")) {
        val local = launchTime.toJavaInstant().atZone(ZoneId.of(zone))
        println("${zone.padEnd(16)} ${local.format(formatter)}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Measuring elapsed time
// ───────────────────────────────────────────────────────────────────────────
// Use a MONOTONIC time source for "how long did it take" — the wall clock can
// jump (NTP sync, daylight saving). `measureTime { }` does that for you.

fun section5MeasuringTime() {
    println("§5")
    val took = measureTime { (1..100_000).sum() }
    println("summing took less than a second: ${took < 1.seconds}")
    val mark = TimeSource.Monotonic.markNow()
    println("elapsed is never negative: ${mark.elapsedNow() >= Duration.ZERO}")
}

// ───────────────────────────────────────────────────────────────────────────
// §6. UUIDs — IDs nobody can guess
// ───────────────────────────────────────────────────────────────────────────
// A UUID is a 128-bit random-looking id, written as 36 characters.
// 🏗️ Why not 1, 2, 3…? Sequential ids leak info ("we have 1042 users") and are
// guessable (/users/1043). Auth tables (module 06) use random string ids.

fun section6Uuids() {
    println("§6")
    val id = Uuid.parse("550e8400-e29b-41d4-a716-446655440000")
    println(id)
    println(id.toHexString())                     // no dashes
    println(Uuid.parseOrNull("not-a-uuid"))
    val random = Uuid.random()
    println("random length ${random.toString().length}, unique: ${random != Uuid.random()}")
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Base64 — bytes as safe text
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Some pipes (URLs, JSON, HTTP headers) only carry "nice" letters. Base64
//    is a secret alphabet of 64 nice characters that can spell ANY bytes.
//    It is NOT encryption — anyone can decode it.
//
// Default alphabet uses `+` and `/` (which break URLs); UrlSafe uses `-` and `_`.

fun section7Base64() {
    println("§7")
    val encoded = Base64.encode("Kotlin".encodeToByteArray())
    println(encoded)
    println(Base64.decode(encoded).decodeToString())
    val tricky = byteArrayOf(-5, -1, -2)
    println("default: ${Base64.Default.encode(tricky)}  url-safe: ${Base64.UrlSafe.encode(tricky)}")
    val noPadding = Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT)
    println("${Base64.UrlSafe.encode("hi".encodeToByteArray())} vs ${noPadding.encode("hi".encodeToByteArray())}")
}

// ───────────────────────────────────────────────────────────────────────────
// §8. Hex — bytes as 0-9a-f
// ───────────────────────────────────────────────────────────────────────────

fun section8Hex() {
    println("§8")
    println(255.toHexString())                    // full Int width: 8 hex digits
    println(255.toByte().toHexString())
    val bytes = "Hi".encodeToByteArray()
    println(bytes.toHexString())
    println("4869".hexToByteArray().decodeToString())
    val macStyle = HexFormat {
        upperCase = true
        bytes { byteSeparator = ":" }
    }
    println(bytes.toHexString(macStyle))
}

// ───────────────────────────────────────────────────────────────────────────
// §9. Putting it together: a session token
// ───────────────────────────────────────────────────────────────────────────
// 🔐 Security (preview of module 06):
//  • Tokens come from `SecureRandom` — NOT `kotlin.random.Random`, whose output can be predicted.
//  • 32 random bytes → URL-safe Base64 without padding → 43-character cookie value.
//  • The database stores only SHA-256(token) as hex. If the DB leaks, attackers
//    get hashes, which they can't turn back into working cookies.

fun newSessionToken(): String {
    val bytes = ByteArray(32).also { SecureRandom().nextBytes(it) }
    return Base64.UrlSafe.withPadding(Base64.PaddingOption.ABSENT).encode(bytes)
}

fun sha256Hex(text: String): String =
    MessageDigest.getInstance("SHA-256").digest(text.encodeToByteArray()).toHexString()

fun section9SessionTokens() {
    println("§9")
    val token = newSessionToken()
    println("token length: ${token.length}, url-safe: ${token.all { it.isLetterOrDigit() || it in "-_" }}")
    println("stored hash length: ${sha256Hex(token).length}")
    println(sha256Hex("hello"))                   // same input → same hash, always
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Duration for "how long" (5.seconds), Instant for "when" (UTC timeline point).
//  • Inject a Clock instead of calling now() directly → testable code.
//  • Time zones only for display: toJavaInstant().atZone(...).
//  • Uuid for unguessable ids; Base64 / hex to turn bytes into text (not encryption!).
//  • Session token = SecureRandom bytes → Base64Url; store only its SHA-256.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// 1h 30m
// 90
// PT1H30M
// 2h 15m
// 1.5s
// true
// §2
// 2026-11-22T10:15:30Z
// 2026-11-24T10:15:30Z
// 1795342530
// 1970-01-01T00:00:00Z
// 1h 44m 30s
// true
// §3
// false
// true
// real clock is after 2026-01-01: true
// §4
// Asia/Kolkata     22 Nov 2026, 15:45
// Europe/London    22 Nov 2026, 10:15
// America/New_York 22 Nov 2026, 05:15
// §5
// summing took less than a second: true
// elapsed is never negative: true
// §6
// 550e8400-e29b-41d4-a716-446655440000
// 550e8400e29b41d4a716446655440000
// null
// random length 36, unique: true
// §7
// S290bGlu
// Kotlin
// default: +//+  url-safe: -__-
// aGk= vs aGk
// §8
// 000000ff
// ff
// 4869
// Hi
// 48:69
// §9
// token length: 43, url-safe: true
// stored hash length: 64
// 2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824
