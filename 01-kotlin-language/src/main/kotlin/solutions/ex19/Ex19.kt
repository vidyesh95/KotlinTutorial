package solutions.ex19

import exercises.ex19.Ex19
import exercises.ex19.IdGenerator
import exercises.ex19.Rgb
import exercises.ex19.Theme

object Ex19Solution : Ex19 {
    override fun idGenerator(prefix: String): IdGenerator {
        var counter = 0
        return object : IdGenerator {
            override fun next() = "$prefix-${++counter}"
        }
    }

    override fun parseHex(hex: String): Rgb? {
        if (hex.length != 7 || !hex.startsWith("#")) return null
        val parts = hex.drop(1).chunked(2).map { it.toIntOrNull(16) ?: return null }
        return Rgb(parts[0], parts[1], parts[2])
    }

    override fun defaultTheme(): Theme = DefaultTheme
}

object DefaultTheme : Theme {
    override val primaryColor = Rgb(0, 122, 255)
}
