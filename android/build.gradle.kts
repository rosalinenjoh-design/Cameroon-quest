buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        // Must match the Compose compiler plugin version in libs.versions.toml.
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.21")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
