// Shared test helpers (e.g. LessonOutputTestBase) used by every lesson module's tests.
plugins {
    id("tutorial.kotlin-jvm")
}

dependencies {
    api(platform(libs.junit.bom))
    api("org.junit.jupiter:junit-jupiter-api")
    api(kotlin("test-junit5"))
}
