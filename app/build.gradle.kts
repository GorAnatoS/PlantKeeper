import java.io.FileInputStream
import java.util.*

plugins{
    id("com.android.application")
    id("kotlin-android")
    id("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    id("dagger.hilt.android.plugin")
}

val keystorePropertiesFile = rootProject.file("keystore.properties.txt")
val keystoreProperties = Properties()
keystoreProperties.load(FileInputStream(keystorePropertiesFile))

android {
    compileSdk = ConfigData.compileSdkVersion
    buildToolsVersion = ConfigData.buildToolsVersion

    buildFeatures {
        dataBinding = true
    }

    defaultConfig {
        applicationId = "com.goranatos.plantkeeper"
        minSdk = ConfigData.minSdkVersion
        targetSdk = ConfigData.targetSdkVersion
        versionCode = ConfigData.versionCode
        versionName = ConfigData.versionName


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

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            configure<com.google.firebase.crashlytics.buildtools.gradle.CrashlyticsExtension> {
                mappingFileUploadEnabled = true
            }

            signingConfig = signingConfigs.getByName("release")
        }

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false

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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(Deps.coreKtx)
    implementation(Deps.coroutinesLibraries)

    implementation(Deps.material)

    implementation(Deps.firebaseLibraries)

    implementation(Deps.hiltAndroid)
    kapt(Deps.hiltAndroidCompiler)

    implementation(Deps.fragmentKtx)
    implementation(Deps.constraintlayout)
    implementation(Deps.appcompat)

    implementation(Deps.preferenceKtx)

    implementation(Deps.lifecycleLibraries)

    implementation(Deps.gson)

    implementation(Deps.roomLibraries)
    annotationProcessor(Deps.roomCompiler)
    kapt(Deps.roomCompiler)

    implementation(Deps.navigationLibraries)

    //3th party libs
    implementation(Deps.glide)
    kapt(Deps.glideCompiler)

    implementation(Deps.groupieLibraries)

    implementation(Deps.lingver)

    implementation(Deps.appIntro)

    implementation(Deps.ucrop)

    implementation(Deps.permissionsDispatcher)
    kapt(Deps.permissionsdispatcherProcessor)
}