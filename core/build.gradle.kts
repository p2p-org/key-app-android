plugins {
    id("org.p2p.wallet.android.library")
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
    api(Dependencies.bitcoinj)

    // RMR input mask
    api(Dependencies.inputmask)

    // https://github.com/JakeWharton/timber
    api(Dependencies.timber)

    implementation(Dependencies.glide)

    // Firebase
    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseCrashlytics)

    // Sentry
    Dependencies.sentryLibraries.forEach { implementation(it) }
}
