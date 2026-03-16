pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
rootProject.name = "Resonance"

include(
    ":app",
    ":core",
    ":data",
    ":emby",
    ":strm",
    ":tv",
    ":poster",
    ":dmg",
    ":largefile",
    ":ui"
)