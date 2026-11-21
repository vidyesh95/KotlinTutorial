plugins {
    id("tutorial.kotlin-jvm")
}

dependencies {
    // Lesson 05 builds a DI container with Kotlin reflection (constructors & parameters).
    implementation(libs.kotlin.reflect)

    // Lesson 06: the real thing — Spring's IoC container (no Spring Boot yet).
    implementation(platform(libs.spring.boot.bom))
    implementation(libs.spring.context)

    testImplementation(project(":test-support"))
}
