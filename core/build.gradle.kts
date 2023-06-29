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
    val koinVersion = "3.2.0"
    // https://github.com/InsertKoinIO/koin
    api("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")

    api("io.insert-koin:koin-androidx-workmanager:$koinVersion")


    // https://github.com/JakeWharton/timber
    val okHttpVersion = "4.9.3"
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")
    api("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    api("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    api("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
    api("com.google.code.gson:gson:$retrofitVersion")

    api(Dependencies.timber)

    implementation(Dependencies.glide)

    // Firebase
    implementation(platform(Dependencies.firebaseBom))
    implementation(Dependencies.firebaseCrashlytics)

    // Sentry
    Dependencies.sentryLibraries.forEach { implementation(it) }
}
