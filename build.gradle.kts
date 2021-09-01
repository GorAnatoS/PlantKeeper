// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        jcenter()
      //  maven(url = "https://maven.google.com") //скопировал с mnemonics
        maven(url = "https://plugins.gradle.org/m2/")
        google()
    }

    dependencies {
        val nav_version = "2.3.5"
        val kotlin_version = "1.5.10"

        val hilt_version = "2.38.1"

        classpath("com.android.tools.build:gradle:4.2.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlin_version")
        classpath("com.google.dagger:hilt-android-gradle-plugin:$hilt_version")

        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:$nav_version")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath("com.google.gms:google-services:4.3.8")

        // Add the dependency for the Performance Monitoring plugin
        classpath("com.google.firebase:perf-plugin:1.4.0") // Performance Monitoring plugin

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("org.jlleitschuh.gradle:ktlint-gradle:10.1.0")

    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}


allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint") // Version should be inherited from parent

    repositories {
        // Required to download KtLint
        mavenCentral()
    }

    // Optionally configure plugin
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
    }
}


task<Delete>("clean") {
    delete(rootProject.buildDir)
}