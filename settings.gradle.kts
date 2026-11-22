// Root of the multi-module build. Each folder below is one "chapter" of the tutorial.
pluginManagement {
    // Our own convention plugins (shared build config) live in build-logic/.
    includeBuild("build-logic")
}

dependencyResolutionManagement {
    // Modules may not declare their own repositories: one source of truth.
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "kotlin-backend-tutorial"

include(
    ":test-support",
    ":01-kotlin-language",
    ":02-dependency-injection",
    ":03-sql-fundamentals",
    ":04-spring-boot-basics",
    ":05-sqldelight",
    ":aoc",
)
