plugins {
    id("tutorial.spring-boot")
    id("tutorial.sqldelight")
}

sqldelight {
    databases {
        // Generates the class `bookstore.db.BookstoreDatabase` + one `…Queries` class per .sq file.
        create("BookstoreDatabase") {
            packageName = "bookstore.db"
            dialect(libs.sqldelight.sqlite.dialect)
            // The schema is DERIVED from the migration files in src/main/sqldelight/migrations —
            // the same files Flyway runs at startup (lesson 03). One source of truth, like Rust's sqlx.
            srcDirs("src/main/sqldelight")
            deriveSchemaFromMigrations = true
            verifyMigrations = true
            // Write plain-SQL copies of the migrations (SQLDelight's `AS Type` removed) for Flyway.
            migrationOutputDirectory = layout.buildDirectory.dir("generated/flyway/db/migration")
            migrationOutputFileFormat = ".sql"
        }
    }
}

// Flyway reads the plain-SQL migration copies from the classpath (db/migration/V1__….sql).
sourceSets.main { resources.srcDir(layout.buildDirectory.dir("generated/flyway")) }
tasks.processResources { dependsOn("generateMainBookstoreDatabaseMigrations") }

springBoot { mainClass = "bookstore.SqlDelightBookstoreApplicationKt" }

dependencies {
    implementation(libs.sqldelight.jdbc.driver)
    implementation(libs.sqldelight.sqlite.driver)
    implementation(libs.sqldelight.coroutines.extensions)        // Query.asFlow() — lesson 04
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.sqlite.jdbc)

    // Lesson 05: SQLDelight inside a Spring Boot app (Flyway + @Transactional + REST).
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation(libs.jackson.module.kotlin)
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(project(":test-support"))
}
