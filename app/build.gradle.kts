import java.io.FileInputStream
import java.util.*

plugins{
    id("com.android.application")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("kotlin-android")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
    id("dagger.hilt.android.plugin")
    id("com.google.gms.google-services")
}

val keystorePropertiesFile = rootProject.file("keystore.properties.txt")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    compileSdk = 30
    buildToolsVersion = "30.0.3"

    buildFeatures {
        viewBinding = true
    }
    defaultConfig {
        applicationId = "com.goranatos.plantkeeper"
        minSdk = 21
        targetSdk = 30
        versionCode = 25
        versionName = "1.3.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        kapt {
            arguments {
                arg("room.schemaLocation", "$projectDir/schemas")
            }
        }
    }

    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin")
        getByName("androidTest").assets.srcDirs("$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            keyAlias = keystoreProperties["keyAlias"] as String
            keyPassword = keystoreProperties["keyPassword"] as String
            storePassword = keystoreProperties["storePassword"] as String
            storeFile = file(keystoreProperties["storeFile"]!!)
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true

            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"

            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = false
            }
        }
    }

    packagingOptions {
        exclude("META-INF/atomicfu.kotlin_module")
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation("com.google.firebase:firebase-perf-ktx:20.0.1")
    implementation("com.google.firebase:firebase-crashlytics-ktx:18.1.0")
    implementation("com.google.firebase:firebase-analytics-ktx:19.0.0")

    val hilt_version = "2.38.1"
    // Hilt dependencies
    implementation("com.google.dagger:hilt-android:$hilt_version")
    kapt("com.google.dagger:hilt-android-compiler:$hilt_version")

    implementation("androidx.fragment:fragment-ktx:1.3.5")

    //coordinator layout
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.1.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")

    //intro
    implementation("com.github.AppIntro:AppIntro:6.1.0")

    //pref
    val preference_version = "1.1.1"
    implementation("androidx.preference:preference-ktx:$preference_version")

    implementation("androidx.core:core-ktx:1.6.0")
    implementation("androidx.appcompat:appcompat:1.3.0")


    val arch_version = "2.3.1"
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:$arch_version")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$arch_version")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$arch_version")

    // Room
    implementation("androidx.room:room-runtime:2.4.0-alpha03")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    // optional - Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.3.0")
    kapt("androidx.room:room-compiler:2.3.0")

    //desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:1.1.5")

    //Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    //Permissions
    val permission_version = "4.8.0"
    implementation("com.github.permissions-dispatcher:permissionsdispatcher:$permission_version")
    annotationProcessor("com.github.permissions-dispatcher:permissionsdispatcher-processor:$permission_version")
    kapt("com.github.permissions-dispatcher:permissionsdispatcher-processor:$permission_version")
    
    // Kotlin Android Coroutines
    val coroutines_version = "1.5.0"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutines_version")
    implementation("com.jakewharton.retrofit:retrofit2-kotlin-coroutines-adapter:0.9.2")

    // Gson
    implementation("com.google.code.gson:gson:2.8.7")

    //translation support
    implementation("com.github.YarikSOffice:lingver:1.3.0")

    //uCrop
    implementation("com.github.yalantis:ucrop:2.2.6")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")


    // New Material Design
    implementation("com.google.android.material:material:1.4.0")

    //Groupie
    val groupie_version = "2.9.0"
    implementation("com.github.lisawray.groupie:groupie:$groupie_version")
    implementation("com.github.lisawray.groupie:groupie-databinding:$groupie_version")
}