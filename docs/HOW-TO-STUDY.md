# How to study with this repo

## 1. Order

Go through the modules in number order (`01` → `08`). Inside a module, go through lessons in number order
(`lesson01_…`, `lesson02_…`). Each lesson builds on the previous one.

## 2. What a lesson looks like

Every lesson is **one runnable file** (`.kt`, or `.sql` + a tiny runner in the SQL module):

```kotlin
/**
 * Lesson 12 — Generic classes
 * Goal: ...            Needs: lesson 11            Time: ~25 min
 */
package lesson12_generic_classes

// §1. The problem generics solve
// ...prose explaining the idea...
//
// 🧒 Like I'm 5: ...a real-world story...
//
// ⚠️ Gotcha: ...the mistake everybody makes...

fun main() {
    section1()   // each §-section is a small function you can read top to bottom
}

// Output:
// ...exactly what `main` prints...
```

Workflow per lesson:

1. **Read** the file top to bottom, like a book chapter.
2. **Predict** what `main` prints before running it.
3. **Run** it (green ▶ in IntelliJ) and compare with the `// Output:` block.
4. **Break it**: change things, see the compiler errors. Errors are teachers.
5. **Do the exercises** (see below).

## 3. Emoji legend

| Mark | Meaning |
|------|---------|
| §1, §2 … | Numbered sections inside a lesson |
| 🧒 **Like I'm 5** | The idea as a simple real-world story, *before* the precise definition |
| ⚠️ **Gotcha** | A trap that bites real developers |
| 🆕 **Kotlin 2.x** | Modern syntax/feature — older tutorials won't show it |
| 🐘 **Postgres note** | How this differs in PostgreSQL (SQL modules) |
| 🔐 **Security** | Something that matters for keeping users safe |
| 🏗️ **In the real world** | Where you will meet this in Spring / backend code |

## 4. Exercises

Every lesson `NN` has an exercise:

```
src/main/kotlin/exercises/exNN/ExNN.kt     <- interface ExNN + object ExNNExercise with TODO() bodies: YOU fill them in
src/main/kotlin/solutions/exNN/ExNN.kt     <- object ExNNSolution: reference answers (peek only after trying!)
src/test/kotlin/exercises/exNN/ExNNTest.kt <- one abstract spec, run against BOTH objects
```

```bash
./gradlew :01-kotlin-language:exercises                          # checks your answers (red until solved — expected)
./gradlew :01-kotlin-language:exercises --tests 'exercises.ex23*' # just one exercise
./gradlew build                                                  # checks the reference solutions, always green
```

The test file runs the same checks against `ExNNExercise` (tagged `exercise`) and `ExNNSolution`.
That "one test, swappable implementation" trick is dependency injection — module 02 explains it properly.

## 5. The lessons are tested too

`LessonOutputTest` runs every lesson's `main()` and compares stdout with the `// Output:` block at the
bottom of the file. So the output shown in a lesson is guaranteed to be real. When you edit a lesson to
experiment, that test will (rightly) fail until you update the block — or just `git checkout` the file.

## 6. Tips

- Type the code yourself instead of only reading — your fingers learn too.
- When a concept feels hard, read its 🧒 box again, then the code, then the box again.
- Keep a `NOTES.md` of things that surprised you. Re-read it weekly.
