plugins {
    id("tutorial.kotlin-jvm")
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":test-support"))
}
