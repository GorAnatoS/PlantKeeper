import org.gradle.api.artifacts.dsl.DependencyHandler

/**
 * To define plugins
 */
object BuildPlugins {
    //GradlePlugins and for classpath in dependencies from build.kotlin.kt(Mnemonics)
    //plugins
    val kotlinGradlePlugin by lazy { "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinGradlePlugin}" }
    val hiltPlugin by lazy { "com.google.dagger:hilt-android-gradle-plugin:${Versions.hilt}" }
    val firebasePerfPlugin by lazy { "com.google.firebase:perf-plugin:${Versions.firebasePerformancePlugin}" }
    val navigationSafeArgsGradlePlugin by lazy { "androidx.navigation:navigation-safe-args-gradle-plugin:${Versions.navigationSafeArgs}" }

    //classpath
    val toolsBuildGradle by lazy { "com.android.tools.build:gradle:${Versions.toolsBuildGradle}" }
    val ktlint by lazy { "org.jlleitschuh.gradle:ktlint-gradle:${Versions.ktLint}" }
    val firebaseCrashliticsGradle by lazy { "com.google.firebase:firebase-crashlytics-gradle:${Versions.firebaseCrashliticsGradle}" }
}


/**
 * To define dependencies
 */
object Deps {
    //android jetpack and google

    val coreKtx by lazy { "androidx.core:core-ktx:1.7.0-alpha01" }

    val material by lazy { "com.google.android.material:material:${Versions.material}" }
    val constraintlayout by lazy { "androidx.constraintlayout:constraintlayout:${Versions.constraintlayout}" }
    val appcompat by lazy { "androidx.appcompat:appcompat:${Versions.appcompat}" }
    val fragmentKtx by lazy { "androidx.fragment:fragment-ktx:${Versions.fragmentKtx}" }

    //lifecycle
    private val lifecycleViewModelKtx by lazy { "androidx.lifecycle:lifecycle-viewmodel-ktx:${Versions.lifecycle}" }
    private val livedataKtx by lazy { "androidx.lifecycle:lifecycle-livedata-ktx:${Versions.lifecycle}" }

    val lifecycleLibraries = arrayListOf<String>().apply {
        add(lifecycleViewModelKtx)
        add(livedataKtx)
    }

    //hilt
    val hiltAndroid by lazy { "com.google.dagger:hilt-android:${Versions.hilt}" }
    val hiltAndroidCompiler by lazy { "com.google.dagger:hilt-android-compiler:${Versions.hilt}" }

    //firebase
    private val firebaseAnalytics by lazy { "com.google.firebase:firebase-analytics:${Versions.firebaseAnalytics}" }
    private val firebaseCrashlytics by lazy { "com.google.firebase:firebase-crashlytics:${Versions.firebaseCrashlytics}" }
    private val firebasePerf by lazy { "com.google.firebase:firebase-perf:${Versions.firebasePerf}" }

    val firebaseLibraries = arrayListOf<String>().apply {
        add(firebaseAnalytics)
        add(firebaseCrashlytics)
        add(firebasePerf)
    }

    //room
    private val roomRuntime by lazy { "androidx.room:room-runtime:${Versions.room}" }
    private val roomKtx by lazy { "androidx.room:room-ktx:${Versions.room}" }
    val roomCompiler by lazy { "androidx.room:room-compiler:${Versions.room}" }
    val roomTesting by lazy { "androidx.room:room-testing:${Versions.room}" }

    val roomLibraries = arrayListOf<String>().apply {
        add(roomRuntime)
        add(roomKtx)
    }

    //navigation
    private val navigationFragmentKtx by lazy { "androidx.navigation:navigation-fragment-ktx:${Versions.navigation}" }
    private val navigationUiKtx by lazy { "androidx.navigation:navigation-ui-ktx:${Versions.navigation}" }

    // Dynamic Feature Module Support
    private val navigationDynamicsFeaturesFragment by lazy { "androidx.navigation:navigation-dynamic-features-fragment:${Versions.navigation}" }

    val navigationLibraries = arrayListOf<String>().apply {
        add(navigationFragmentKtx)
        add(navigationUiKtx)
        add(navigationDynamicsFeaturesFragment)
    }

    // Kotlin Android Coroutines
    private val coroutinesCore by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-core:${Versions.coroutines}" }
    private val coroutinesAndroid by lazy { "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Versions.coroutines}" }

    val coroutinesLibraries = arrayListOf<String>().apply {
        add(coroutinesCore)
        add(coroutinesAndroid)
    }

    // preference Kotlin
    val preferenceKtx by lazy { "androidx.preference:preference-ktx:${Versions.preference}" }

    //DataStore Jetpack
    val datastorePreferences by lazy { "androidx.datastore:datastore-preferences:1.0.0" }

    //3td-party Libs

    //groupie
    private val groupie by lazy { "com.xwray:groupie:${Versions.groupie}" }
    private val groupieDataBinding by lazy { "com.xwray:groupie-databinding:${Versions.groupie}" }

    val groupieLibraries = arrayListOf<String>().apply {
        add(groupie)
        add(groupieDataBinding)
    }

    //glide
    val glide by lazy { "com.github.bumptech.glide:glide:${Versions.glide}" }
    val glideCompiler by lazy { "com.github.bumptech.glide:compiler:${Versions.glide}" }

    //intro
    val appIntro by lazy { "com.github.AppIntro:AppIntro:6.1.0" }

    //translation support
    val lingver by lazy { "com.github.YarikSOffice:lingver:1.3.0" }

    //Permissions
    val permissionsDispatcher by lazy { "com.github.permissions-dispatcher:permissionsdispatcher:${Versions.permission}" }
    val permissionsdispatcherProcessor by lazy { "com.github.permissions-dispatcher:permissionsdispatcher-processor:${Versions.permission}" }

    //uCrop
    val ucrop by lazy { "com.github.yalantis:ucrop:${Versions.ucrop}" }

    // Gson
    val gson by lazy { "com.google.code.gson:gson:${Versions.gson}" }
}

//util functions for adding the different type dependencies from build.gradle file
fun DependencyHandler.kapt(list: List<String>) {
    list.forEach { dependency ->
        add("kapt", dependency)
    }
}

fun DependencyHandler.implementation(list: List<String>) {
    list.forEach { dependency ->
        add("implementation", dependency)
    }
}

fun DependencyHandler.annotationProcessor(list: List<String>) {
    list.forEach { dependency ->
        add("annotationProcessor", dependency)
    }
}

fun DependencyHandler.androidTestImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("androidTestImplementation", dependency)
    }
}

fun DependencyHandler.testImplementation(list: List<String>) {
    list.forEach { dependency ->
        add("testImplementation", dependency)
    }
}