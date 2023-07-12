plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
}

apply {
    from("${project.rootDir}/.scripts/torus.gradle")
    from("${project.rootDir}/.scripts/config.gradle")
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
    implementation(Dependencies.firebaseCrashlytics)

    // Sentry
    Dependencies.sentryLibraries.forEach { implementation(it) }
}
