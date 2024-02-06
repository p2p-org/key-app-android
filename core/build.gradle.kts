@file:Suppress("UnstableApiUsage")

plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
}

koverReport {
    androidReports("debug") {
        filters {
            excludes {
                annotatedBy(
                    "*NoCoverage",
                    "*Generated*",
                    "androidx.room.Database",
                )
                packages(
                    "com.bumptech.glide*",
                )
                classes(
                    "*Module",
                    "*Module\$*",
                    "*Interceptor",
                    "*Interceptor\$*",
                    "*.BuildConfig",

                    // excludes debug classes
                    "*.DebugUtil"
                )
            }
        }
    }
}

apply {
    from("${project.rootDir}/.scripts/torus.gradle")
    from("${project.rootDir}/.scripts/config.gradle")
}

android {
    buildFeatures.buildConfig = true
}

dependencies {
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }

    // Koin
    Dependencies.koinLibraries.forEach { api(it) }

    // Retrofit
    Dependencies.retrofitLibraries.forEach { api(it) }

    // Tinder Scarlet
    Dependencies.tinderScarletLibraries.forEach { implementation(it) }

    // bitcoinj
    val koinVersion = "3.2.0"
    // https://github.com/InsertKoinIO/koin
    api("com.google.code.gson:gson:2.9.0")
    api(Dependencies.inputmask)
    api(Dependencies.timber)
    implementation(Dependencies.glide)
    implementation(Dependencies.bitcoinj)

    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)

    // Firebase
    implementation(platform(Dependencies.firebaseBom))
    Dependencies.firebaseLibraries.forEach { implementation(it) }

    Dependencies.appsFlyerLibraries.forEach { implementation(it) }

    // analytics
    implementation(Dependencies.amplitude)

    // Sentry
    Dependencies.sentryLibraries.forEach { implementation(it) }
}
