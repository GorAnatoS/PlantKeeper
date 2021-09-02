// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        mavenCentral()
        jcenter()
        maven(url = "https://plugins.gradle.org/m2/")
        google()
    }

    dependencies {
        classpath(BuildPlugins.kotlinGradlePlugin)
        classpath(BuildPlugins.toolsBuildGradle)
        classpath(BuildPlugins.navigationSafeArgsGradlePlugin)
        classpath(BuildPlugins.firebaseCrashliticsGradle)
        classpath(BuildPlugins.firebasePerfPlugin)
        classpath(BuildPlugins.hiltPlugin)
        classpath(BuildPlugins.ktlint)

    }
}

plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
}


allprojects {
    repositories {
        google()
        jcenter()
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