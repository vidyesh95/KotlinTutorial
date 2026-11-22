plugins {
    id("tutorial.spring-boot")
}

dependencies {
    // Web MVC (REST controllers + Tomcat), Validation (@Email, @NotBlank), JDBC (DataSource, JdbcClient, @Transactional)
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-security")
    // Bouncy Castle provider required by Spring Security for Argon2id hashing (Better-Auth standard)
    implementation("org.bouncycastle:bcprov-jdk18on:1.80")
    implementation(libs.jackson.module.kotlin)
    implementation(libs.sqlite.jdbc)

    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(project(":test-support"))
}
