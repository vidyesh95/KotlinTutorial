# Lesson 02 — Passwords and Hashing: BCrypt to Argon2id

| | |
|---|---|
| **Goal** | Understand why general-purpose hashing functions (MD5, SHA-256) fail for passwords, how the Password Hashing Competition chose **Argon2id** (the Better-Auth standard), and how to configure Spring's `DelegatingPasswordEncoder` with Argon2id and BCrypt. |
| **Needs** | Lesson 01 |
| **Time** | ~30 min |
| **Next** | [03 — The Better-Auth Schema](03-better-auth-schema.md) |
| **Exercise** | `src/main/kotlin/exercises/ex01/` |

---

## §1. The graveyard of broken password schemes

1. **Plaintext**:
   Storing `"password123"` directly in a database column. One SQL injection or database backup leak exposes every user's password across every website where they reused it.
2. **MD5 / SHA-1**:
   Completely broken. Precomputed "rainbow tables" reverse arbitrary MD5 hashes in milliseconds.
3. **Unsalted SHA-256 / SHA-512**:
   Fast cryptographic hash functions designed to digest gigabytes of files per second. A modern GPU (e.g. RTX 4090) computes over **10 billion SHA-256 hashes per second**. An attacker brute-forces standard 8-character passwords in minutes.

🔐 **Security Rule #1:** A good cryptographic hash (like SHA-256) is **FAST**. A good password hash MUST be **SLOW** and **ADAPTIVE**.

---

## §2. BCrypt: The 1999 pioneer

In 1999, Niels Provos and David Mazières introduced BCrypt based on the Blowfish cipher:
- **Salt**: 16 bytes of random data prepended to every password to invalidate rainbow tables.
- **Adaptive Work Factor**: A configurable cost ($2^{\text{cost}}$ iterations).

However, BCrypt has two physical limitations in the modern era:
1. **CPU-bound, not memory-bound**: BCrypt uses only 4 KB of memory during hashing. Dedicated ASIC and FPGA cracking rigs can cram thousands of BCrypt cores onto a single silicon chip to attack leaked databases.
2. **72-byte truncation**: BCrypt silently truncates passwords longer than 72 bytes.

---

## §3. Argon2id: The Better-Auth standard

In 2015, the international **Password Hashing Competition (PHC)** concluded with **Argon2** winning first place.

Argon2 comes in three variants:
- `Argon2d`: Data-dependent memory access (resists GPU attacks, but vulnerable to side-channel cache attacks).
- `Argon2i`: Data-independent memory access (resists side-channel attacks).
- **`Argon2id`**: A hybrid combining both (data-independent for the first pass against side channels, data-dependent for subsequent passes against GPUs/ASICs).

```
 $argon2id$v=19$m=16384,t=2,p=1$DI+Ohn0u9IBNv9Ml4bGJVQ$TKIT970MRb8blo2FF8IkYq2hLmvdDxC04pdljhW8CLQ
 ───────── ──── ─────────────── ────────────────────── ───────────────────────────────────────────
     │       │         │                  │                                 │
     │       │         │                  │                                 └─ 32-byte derived key
     │       │         │                  └─ 16-byte random salt
     │       │         └─ parameters: m=16MB memory, t=2 iterations, p=1 thread
     │       └─ version (19 = v1.3)
     └─ algorithm: Argon2id
```

### Why Better-Auth defaults to Argon2id:
Because of **memory hardness** ($m=16384 = 16\text{ MiB}$). An ASIC chip cannot run thousands of instances in parallel without running out of on-chip RAM, eliminating the hardware advantage attackers previously held.

---

## §4. Spring's `DelegatingPasswordEncoder`

In Java, standard OpenJDK does not bundle Argon2 in `java.security`. We add the **Bouncy Castle** provider (`org.bouncycastle:bcprov-jdk18on`) in [`build.gradle.kts`](../build.gradle.kts).

To support multiple hashing schemes cleanly, Spring Security provides **`DelegatingPasswordEncoder`**:

In [`src/main/kotlin/bookstore/auth/security/PasswordConfig.kt`](../src/main/kotlin/bookstore/auth/security/PasswordConfig.kt):

```kotlin
@Configuration
class PasswordConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        val argon2 = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8()
        val bcrypt = BCryptPasswordEncoder(10)

        val encoders: MutableMap<String, PasswordEncoder> = mutableMapOf(
            "argon2" to argon2,
            "bcrypt" to bcrypt,
        )

        // Argon2id is the default algorithm used for new hashes (matching Better-Auth)
        val delegating = DelegatingPasswordEncoder("argon2", encoders)
        // If an existing hash has no prefix, fall back to argon2
        delegating.setDefaultPasswordEncoderForMatches(argon2)

        return delegating
    }
}
```

### The superpower of prefix-tagged hashes:
- New sign-ups produce: `{argon2}$argon2id$v=19$m=16384,t=2,p=1$...`
- If an older user database had `{bcrypt}$2a$10$...`, `passwordEncoder.matches()` automatically detects `{bcrypt}` and verifies it with BCrypt!
- This enables seamless zero-downtime password migration without forcing everyone to reset passwords.

---

## §5. Timing attacks and constant-time comparison

Whether using BCrypt or Argon2id, verifying password hashes must never short-circuit character by character:
```kotlin
// Defeats network latency timing attacks:
MessageDigest.isEqual(inputBytes, actualBytes)
```
Spring Security's `PasswordEncoder.matches(raw, hash)` does this constant-time comparison automatically.

Try exercise 01:
```bash
./gradlew :06-spring-security-auth:exercises --tests 'exercises.ex01*'
```
