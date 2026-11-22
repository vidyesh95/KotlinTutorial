# Module 06 Exercises — Spring Security & Better-Auth

One exercise per security discipline. Each exercise lives in `exercises/exNN/` (your code to implement) with a twin in `solutions/exNN/` (reference answers).

- **Your endpoints** are served under `/exercises/exNN/...`, the reference ones under `/solutions/exNN/...`.
- **Tests** in `src/test/kotlin/exercises/exNN/` run the SAME checks against both paths:
  `ExNNSolutionTest` (runs as part of the normal build, always green) and `ExNNExerciseTest` (tagged `exercise`).
- Unsolved `TODO()` endpoints answer **500** until implemented.

```bash
./gradlew :06-spring-security-auth:exercises --tests 'exercises.ex01*'   # test one exercise
./gradlew :06-spring-security-auth:exercises                            # test all your exercises
./gradlew :06-spring-security-auth:test                                 # app integration tests + solutions (always green)
```

| Exercise | Topic | Endpoints |
|---|---|---|
| `ex01` | Password Hashing | `POST /hash`, `POST /verify` |
| `ex02` | Session Validation & Touch | `POST /validate`, `POST /extend` |
| `ex03` | Role & Ownership Guards | `GET /admin-ping`, `GET /user-data/{userId}` |
| `ex04` | Session Revocation | `POST /revoke-others` |
