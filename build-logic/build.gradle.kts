plugins {
    // Lets us write convention plugins as `*.gradle.kts` files in src/main/kotlin.
    `kotlin-dsl`
}

dependencies {
    // Plugins that the convention plugins apply must be on this build's classpath.
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.allopen)             // provides kotlin("plugin.spring")
    implementation(libs.spring.boot.gradle.plugin)
}
