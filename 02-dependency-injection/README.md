# 02 — Dependency Injection

> 🧒 You don't cook your own lunch at school — someone packs your lunchbox and hands it to you.
> Classes shouldn't build their own helpers either. That's all DI is. The rest is detail.

Six lessons that take DI from "what is it?" to building your own container, and then to Spring's container.
After this module, Spring stops being magic: you'll know what `@Component`, `@Bean`, `@Primary`, scopes
and "circular dependency" errors are, because you'll have built them yourself.

```bash
./gradlew :02-dependency-injection:lesson -Plesson=lesson05_build_a_container.BuildAContainerKt
./gradlew :02-dependency-injection:exercises --tests 'exercises.ex05*'
./gradlew :02-dependency-injection:test
```

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 01 | [What is a dependency? (the lunchbox)](src/main/kotlin/lesson01_what_is_a_dependency/WhatIsADependency.kt) | [ex01](src/main/kotlin/exercises/ex01/Ex01.kt) — inject instead of `new` |
| ☐ | 02 | [Constructor injection, interfaces and fakes](src/main/kotlin/lesson02_constructor_injection/ConstructorInjection.kt) | [ex02](src/main/kotlin/exercises/ex02/Ex02.kt) — write fakes, inject time |
| ☐ | 03 | [The composition root and object lifetimes](src/main/kotlin/lesson03_composition_root/CompositionRoot.kt) | [ex03](src/main/kotlin/exercises/ex03/Ex03.kt) — a lazy module: singleton vs transient |
| ☐ | 04 | [Anti-patterns: service locator, over-injection, cycles](src/main/kotlin/lesson04_anti_patterns/AntiPatterns.kt) | [ex04](src/main/kotlin/exercises/ex04/Ex04.kt) — cycle detection & creation order |
| ☐ | 05 | [Build your own DI container](src/main/kotlin/lesson05_build_a_container/BuildAContainer.kt) + [Container.kt](src/main/kotlin/lesson05_build_a_container/Container.kt) | [ex05](src/main/kotlin/exercises/ex05/Ex05.kt) — autowiring with reflection |
| ☐ | 06 | [The real thing: Spring's IoC container](src/main/kotlin/lesson06_spring_container/SpringContainer.kt) | [ex06](src/main/kotlin/exercises/ex06/Ex06.kt) — beans, `@Primary`, prototype scope |

Concepts from module 01 you'll use here: interfaces (16), `by lazy` (21), generics & `reified` (23–27),
context parameters (28 — a lightweight alternative for "ambient" dependencies), DSLs (29), testing with fakes (35).

Next: `03-sql-fundamentals`.
