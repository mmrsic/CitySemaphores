rootProject.name = "CitySemaphores"

// Enable Gradle feature previews for improved build performance and type-safe project accessors
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    // Note: repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) is not used
    // because Kotlin/JS plugin adds Node.js distribution repository at project level

    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Include composeApp module - contains multiplatform targets:
// - JS IR (Browser-based version)
// - Android (Mobile application)
// - JVM Desktop (Desktop application)
// - WasmJs (WebAssembly experimental)
include(":composeApp")
