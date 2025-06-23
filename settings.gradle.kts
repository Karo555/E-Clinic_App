// settings.gradle.kts

pluginManagement {
    repositories {
        // Google’s Maven repository (for Firebase, AndroidX, etc.)
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        // Maven Central (for kotlinx-serialization, etc.)
        mavenCentral()
        // Gradle Plugin Portal (for other Gradle plugins)
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Required so Gradle can fetch Firebase & Data Connect artifacts
        google()
        // Required so Gradle can fetch kotlinx-serialization, etc.
        mavenCentral()
    }
}

rootProject.name = "E-Clinic_App"
include(":app")