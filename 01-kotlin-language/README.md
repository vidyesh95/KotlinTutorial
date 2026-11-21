# 01 — Kotlin language

35 lessons that take you from `println("Hello")` to generics, coroutines and DSLs — everything the Spring
modules later rely on. Lesson order follows the Hyperskill Kotlin track (Basics → Types & data → Control flow →
OOP → Generics → Additional instruments) and adds modern Kotlin 2.4 features (🆕).

Read [../docs/HOW-TO-STUDY.md](../docs/HOW-TO-STUDY.md) first.

```bash
./gradlew :01-kotlin-language:lesson -Plesson=lesson23_generics_intro.GenericsIntroKt   # run one lesson
./gradlew :01-kotlin-language:exercises --tests 'exercises.ex23*'                        # check your exercise
./gradlew :01-kotlin-language:test                                                       # lessons + solutions (always green)
```

## Part A — Basics & data

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 01 | [Hello, World: the anatomy of a Kotlin program](src/main/kotlin/lesson01_hello_world/HelloWorld.kt) | [ex01](src/main/kotlin/exercises/ex01/Ex01.kt) |
| ☐ | 02 | [Variables: `val`, `var`, types and scope](src/main/kotlin/lesson02_variables/Variables.kt) | [ex02](src/main/kotlin/exercises/ex02/Ex02.kt) |
| ☐ | 03 | [Numbers, Booleans, Chars and conversions](src/main/kotlin/lesson03_numbers_and_types/NumbersAndTypes.kt) | [ex03](src/main/kotlin/exercises/ex03/Ex03.kt) |
| ☐ | 04 | [Strings and Chars](src/main/kotlin/lesson04_strings_and_chars/StringsAndChars.kt) | [ex04](src/main/kotlin/exercises/ex04/Ex04.kt) |
| ☐ | 05 | [Input and output: talking to the user](src/main/kotlin/lesson05_input_output/InputOutput.kt) | [ex05](src/main/kotlin/exercises/ex05/Ex05.kt) |
| ☐ | 06 | [Null safety: the "billion-dollar mistake", fixed](src/main/kotlin/lesson06_null_safety/NullSafety.kt) | [ex06](src/main/kotlin/exercises/ex06/Ex06.kt) |
| ☐ | 07 | [Collections: arrays, lists, sets, maps and sequences](src/main/kotlin/lesson07_collections/Collections.kt) | [ex07](src/main/kotlin/exercises/ex07/Ex07.kt) |

## Part B — Control flow & functions

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 08 | [Making decisions: `if` and `when`](src/main/kotlin/lesson08_if_and_when/IfAndWhen.kt) | [ex08](src/main/kotlin/exercises/ex08/Ex08.kt) |
| ☐ | 09 | [Repeating things: loops and ranges](src/main/kotlin/lesson09_loops_and_ranges/LoopsAndRanges.kt) | [ex09](src/main/kotlin/exercises/ex09/Ex09.kt) |
| ☐ | 10 | [When things go wrong: exceptions and Result](src/main/kotlin/lesson10_exceptions_and_result/ExceptionsAndResult.kt) | [ex10](src/main/kotlin/exercises/ex10/Ex10.kt) |
| ☐ | 11 | [Functions](src/main/kotlin/lesson11_functions/Functions.kt) | [ex11](src/main/kotlin/exercises/ex11/Ex11.kt) |
| ☐ | 12 | [Lambdas and function types](src/main/kotlin/lesson12_lambdas_and_function_types/LambdasAndFunctionTypes.kt) | [ex12](src/main/kotlin/exercises/ex12/Ex12.kt) |
| ☐ | 13 | [Higher-order functions and `inline`](src/main/kotlin/lesson13_higher_order_and_inline/HigherOrderAndInline.kt) | [ex13](src/main/kotlin/exercises/ex13/Ex13.kt) |
| ☐ | 14 | [Scope functions: let, run, with, apply, also](src/main/kotlin/lesson14_scope_functions/ScopeFunctions.kt) | [ex14](src/main/kotlin/exercises/ex14/Ex14.kt) |

## Part C — Object-oriented programming

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 15 | [Classes and properties](src/main/kotlin/lesson15_classes_and_properties/ClassesAndProperties.kt) | [ex15](src/main/kotlin/exercises/ex15/Ex15.kt) |
| ☐ | 16 | [Inheritance, abstract classes, interfaces, visibility](src/main/kotlin/lesson16_inheritance_and_interfaces/InheritanceAndInterfaces.kt) | [ex16](src/main/kotlin/exercises/ex16/Ex16.kt) |
| ☐ | 17 | [Data classes, enum classes, value classes](src/main/kotlin/lesson17_data_enum_value_classes/DataEnumValueClasses.kt) | [ex17](src/main/kotlin/exercises/ex17/Ex17.kt) |
| ☐ | 18 | [Sealed hierarchies](src/main/kotlin/lesson18_sealed_hierarchies/SealedHierarchies.kt) | [ex18](src/main/kotlin/exercises/ex18/Ex18.kt) |
| ☐ | 19 | [Objects, companion objects, nested and inner classes](src/main/kotlin/lesson19_objects_and_nesting/ObjectsAndNesting.kt) | [ex19](src/main/kotlin/exercises/ex19/Ex19.kt) |
| ☐ | 20 | [Extensions, infix functions, operator overloading](src/main/kotlin/lesson20_extensions_and_operators/ExtensionsAndOperators.kt) | [ex20](src/main/kotlin/exercises/ex20/Ex20.kt) |
| ☐ | 21 | [Delegation: `by` for classes and properties](src/main/kotlin/lesson21_delegation/Delegation.kt) | [ex21](src/main/kotlin/exercises/ex21/Ex21.kt) |
| ☐ | 22 | [The type system: Any, Unit, Nothing, checks, casts, equality](src/main/kotlin/lesson22_type_system/TypeSystem.kt) | [ex22](src/main/kotlin/exercises/ex22/Ex22.kt) |

## Part D — Generics (the deep dive)

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 23 | [Generics, part 1: boxes with labels](src/main/kotlin/lesson23_generics_intro/GenericsIntro.kt) | [ex23](src/main/kotlin/exercises/ex23/Ex23.kt) |
| ☐ | 24 | [Generics, part 2: constraints (upper bounds)](src/main/kotlin/lesson24_generic_constraints/GenericConstraints.kt) | [ex24](src/main/kotlin/exercises/ex24/Ex24.kt) |
| ☐ | 25 | [Generics, part 3: variance (`out` and `in`)](src/main/kotlin/lesson25_variance/Variance.kt) | [ex25](src/main/kotlin/exercises/ex25/Ex25.kt) |
| ☐ | 26 | [Generics, part 4: use-site variance and star projections](src/main/kotlin/lesson26_use_site_variance_and_star/UseSiteVarianceAndStar.kt) | [ex26](src/main/kotlin/exercises/ex26/Ex26.kt) |
| ☐ | 27 | [Generics, part 5: type erasure and `reified`](src/main/kotlin/lesson27_reified_and_erasure/ReifiedAndErasure.kt) | [ex27](src/main/kotlin/exercises/ex27/Ex27.kt) |

## Part E — Advanced Kotlin & the backend toolbox

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 28 | [Context parameters: helpers that are "always in the room"](src/main/kotlin/lesson28_context_parameters/ContextParameters.kt) | [ex28](src/main/kotlin/exercises/ex28/Ex28.kt) |
| ☐ | 29 | [Lambdas with receiver and DSLs](src/main/kotlin/lesson29_dsls_and_receivers/DslsAndReceivers.kt) | [ex29](src/main/kotlin/exercises/ex29/Ex29.kt) |
| ☐ | 30 | [Coroutines, part 1: doing many things while waiting](src/main/kotlin/lesson30_coroutines_basics/CoroutinesBasics.kt) | [ex30](src/main/kotlin/exercises/ex30/Ex30.kt) |
| ☐ | 31 | [Coroutines, part 2: cancellation, errors, Flow](src/main/kotlin/lesson31_coroutines_flow_and_cancellation/FlowAndCancellation.kt) | [ex31](src/main/kotlin/exercises/ex31/Ex31.kt) |
| ☐ | 32 | [Time, IDs and encoding: the backend toolbox](src/main/kotlin/lesson32_time_ids_encoding/TimeIdsEncoding.kt) | [ex32](src/main/kotlin/exercises/ex32/Ex32.kt) |
| ☐ | 33 | [Regular expressions: patterns for text](src/main/kotlin/lesson33_regex/Regex.kt) | [ex33](src/main/kotlin/exercises/ex33/Ex33.kt) |
| ☐ | 34 | [Files and I/O](src/main/kotlin/lesson34_files_and_io/FilesAndIo.kt) | [ex34](src/main/kotlin/exercises/ex34/Ex34.kt) |
| ☐ | 35 | [Testing your Kotlin code](src/main/kotlin/lesson35_testing/Testing.kt) | [ex35](src/main/kotlin/exercises/ex35/Ex35.kt) |

🧒 Hardest topics, with the most "like I'm 5" help: **null safety (06)**, **lambdas (12)**, **inline (13)**,
**variance (25)**, **reified (27)**, **context parameters (28)**, **coroutines (30–31)**. Take them slowly.

After this module: practise in [`aoc`](../aoc), then continue with `02-dependency-injection`.
