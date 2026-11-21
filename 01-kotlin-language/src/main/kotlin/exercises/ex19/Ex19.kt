package exercises.ex19

/*
 * Exercise 19 — Objects and nesting          (lesson: lesson19_objects_and_nesting)
 *
 * Replace every TODO(...) with real code, then run:
 *     ./gradlew :01-kotlin-language:exercises
 * Stuck? Re-read the lesson first. The answer is in solutions/ex19/Ex19.kt.
 */

interface IdGenerator {
    fun next(): String
}

data class Rgb(val red: Int, val green: Int, val blue: Int)

interface Theme {
    val primaryColor: Rgb
}

interface Ex19 {
    /**
     * 19.1 — Return a NEW generator producing "<prefix>-1", "<prefix>-2", …
     * Two generators must count independently. Hint: an object expression `object : IdGenerator { … }`
     * capturing a local `var`.
     */
    fun idGenerator(prefix: String): IdGenerator

    /**
     * 19.2 — Parse "#RRGGBB" (hex, case-insensitive) → Rgb. "#FF8800" → Rgb(255, 136, 0).
     * Anything else (missing '#', wrong length, non-hex) → null.
     * Hint: "FF".toIntOrNull(16).
     */
    fun parseHex(hex: String): Rgb?

    /**
     * 19.3 — Return a Theme whose primaryColor is Rgb(0, 122, 255).
     * Every call must return the SAME instance. Hint: `object DefaultTheme : Theme`.
     */
    fun defaultTheme(): Theme
}

object Ex19Exercise : Ex19 {
    override fun idGenerator(prefix: String): IdGenerator = TODO("19.1")
    override fun parseHex(hex: String): Rgb? = TODO("19.2")
    override fun defaultTheme(): Theme = TODO("19.3")
}
