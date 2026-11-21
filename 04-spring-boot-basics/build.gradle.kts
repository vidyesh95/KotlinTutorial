plugins {
    id("tutorial.spring-boot")
}

dependencies {
    // Each "starter" = a curated bundle of libraries + auto-configuration (lesson 01).
    implementation("org.springframework.boot:spring-boot-starter-webmvc")      // REST controllers + embedded Tomcat
    implementation("org.springframework.boot:spring-boot-starter-validation")  // @NotBlank, @Positive … (Jakarta Validation)
    implementation("org.springframework.boot:spring-boot-starter-jdbc")        // DataSource, HikariCP pool, JdbcClient, @Transactional
    implementation("org.springframework.boot:spring-boot-starter-flyway")      // versioned SQL migrations
    implementation("org.springframework.boot:spring-boot-starter-actuator")    // /actuator/health, metrics, info
    implementation(libs.jackson.module.kotlin)                                 // JSON <-> Kotlin data classes
    implementation(libs.sqlite.jdbc)                                           // the database driver

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
}

// `./gradlew :04-spring-boot-basics:bootRun` starts the app on http://localhost:8080
