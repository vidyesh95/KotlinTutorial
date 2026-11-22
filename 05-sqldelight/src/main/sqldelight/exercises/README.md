# SQLDelight exercises

You write **real SQL** in `.sq` files; SQLDelight turns them into Kotlin functions and the tests call those.

## The placeholder convention

A `.sq` query can't contain `TODO()`, and a missing query wouldn't compile. So every exercise query is a
**placeholder** that already has its final *signature* — same label, same parameter names and types, same result
columns — but returns nothing, usually thanks to `WHERE 0` / `AND 0`:

```sql
-- 02.1 TODO: id and title of every book written by :authorName, by title A → Z.
booksByAuthor:
SELECT books.id, books.title
FROM books
JOIN authors ON authors.id = books.author_id
WHERE authors.name = :authorName AND 0      -- ← remove the trick, write the real query
ORDER BY books.title;
```

Rules: keep the label, keep `:parameter` names, keep the result column names/types. If you change them, the test
file stops compiling — which is SQLDelight doing its job. 🙂

Some exercises (03, 04, 05) are Kotlin: those live in `src/main/kotlin/exercises/exNN/` with `TODO("NN.k")` bodies.
Reference answers: `src/main/sqldelight/solutions/` and `src/main/kotlin/solutions/`.

```bash
./gradlew :05-sqldelight:exercises --tests 'exercises.ex02*'   # check one exercise (red until solved)
./gradlew :05-sqldelight:test                                  # reference solutions, always green
```
