// Convention plugin for Spring Boot modules: `plugins { id("tutorial.spring-boot") }`.

import org.springframework.boot.gradle.plugin.SpringBootPlugin

plugins {
    id("tutorial.kotlin-jvm")
    id("org.springframework.boot")
    // Kotlin classes are final; Spring must subclass some of them (proxies for @Configuration,
    // @Transactional, …). This compiler plugin opens classes annotated with Spring annotations.
    id("org.jetbrains.kotlin.plugin.spring")
}

dependencies {
    // Spring Boot's BOM picks compatible versions for hundreds of libraries — no versions needed below.
    implementation(platform(SpringBootPlugin.BOM_COORDINATES))
    testImplementation(platform(SpringBootPlugin.BOM_COORDINATES))
    // Spring uses Kotlin reflection to understand constructors, nullability and default values.
    implementation("org.jetbrains.kotlin:kotlin-reflect")
}

// sqlite-jdbc loads a native library; JDK 25 warns unless native access is allowed.
tasks.withType<Test>().configureEach { jvmArgs("--enable-native-access=ALL-UNNAMED") }
tasks.withType<JavaExec>().configureEach { jvmArgs("--enable-native-access=ALL-UNNAMED") }
