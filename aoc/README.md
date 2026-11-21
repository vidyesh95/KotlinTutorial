# Advent of Code playground

Each day folder in `src/main/resources/advent_of_code/year2021/dayN/` has:

- `DayN.txt` — the puzzle description (committed).
- `input.txt` — **your** puzzle input. Download it from adventofcode.com (logged in) and save it there.
  It is git-ignored: AoC asks people not to publish their inputs.

```kotlin
package aoc.year2021

import aoc.readInput

fun main() {
    val depths = readInput(year = 2021, day = 1).map(String::toInt)
    println(depths.zipWithNext().count { (a, b) -> b > a })
}
```

Create one file per day in `src/main/kotlin/aoc/year2021/` and run it with the green ▶ in IntelliJ, or:

```bash
./gradlew :aoc:lesson -Plesson=aoc.year2021.Day01Kt
```

Good practice after finishing each block of `01-kotlin-language`.
