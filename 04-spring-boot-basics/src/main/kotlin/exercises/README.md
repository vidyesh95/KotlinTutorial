# Module 04 exercises

One exercise per lesson. Each lives in `exercises/exNN/` (your code) with a twin in `solutions/exNN/`
(reference answer — peek only after trying).

- **Your endpoints** are served under `/exercises/exNN/...`, the reference ones under `/solutions/exNN/...`.
  Both run inside the same app, so you can compare them live after `./gradlew :04-spring-boot-basics:bootRun`.
- **Tests** in `src/test/kotlin/exercises/exNN/` run the SAME checks against both paths:
  `ExNNSolutionTest` (part of the normal build, always green) and `ExNNExerciseTest` (tagged `exercise`).
- Unsolved `TODO()` endpoints answer **500** — that's expected until you implement them.
- Exercise 05 uses the `reviews` table from `db/migration/V3__reviews.sql`; exercise 04 reads
  `exercises.shipping.*` from `application.yml`; exercise 07 is about writing tests (it lives only in `src/test`).
- The app scans these packages because `BookstoreApplication` lists them in `scanBasePackages`.

```bash
./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex02*'   # one exercise
./gradlew :04-spring-boot-basics:exercises                            # all of them (red until solved)
./gradlew :04-spring-boot-basics:test                                 # app tests + reference solutions (green)
```
