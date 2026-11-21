plugins {
    id("tutorial.kotlin-jvm")
}

dependencies {
    // SQLite in-process database + its JDBC driver. No server to install.
    implementation(libs.sqlite.jdbc)

    testImplementation(project(":test-support"))
}

// sqlite-jdbc loads a native library; JDK 25 warns unless native access is allowed.
tasks.withType<Test>().configureEach { jvmArgs("--enable-native-access=ALL-UNNAMED") }
tasks.withType<JavaExec>().configureEach { jvmArgs("--enable-native-access=ALL-UNNAMED") }
