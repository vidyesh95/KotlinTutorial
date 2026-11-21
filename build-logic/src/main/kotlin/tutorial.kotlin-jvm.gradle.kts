// Convention plugin applied by every Kotlin module: `plugins { id("tutorial.kotlin-jvm") }`.

import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("org.jetbrains.kotlin.jvm")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

kotlin {
    jvmToolchain(25)
}

dependencies {
    // kotlin("test") picks kotlin-test-junit5 automatically because we use the JUnit Platform.
    testImplementation(kotlin("test"))
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
}

// `./gradlew test` -> everything EXCEPT your unfinished exercises, so the build stays green.
tasks.test {
    useJUnitPlatform {
        excludeTags("exercise")
    }
}

// `./gradlew :01-kotlin-language:exercises` -> only tests tagged "exercise" (your homework).
tasks.register<Test>("exercises") {
    group = "tutorial"
    description = "Checks YOUR exercise answers (tests tagged 'exercise')."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform {
        includeTags("exercise")
    }
    testLogging {
        events("passed", "failed")
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// `./gradlew :01-kotlin-language:lesson -Plesson=lesson01.HelloKt` -> run one lesson's main().
// (In IntelliJ just click the green ▶ next to `fun main()`.)
tasks.register<JavaExec>("lesson") {
    group = "tutorial"
    description = "Runs one lesson: -Plesson=<fully.qualified.FileNameKt>"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = providers.gradleProperty("lesson")
    standardInput = System.`in`
}
