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

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")

    // https://mvnrepository.com/artifact/org.bitcoinj/bitcoinj-core
    api("org.bitcoinj:bitcoinj-core:0.15.10")
    // https://mvnrepository.com/artifact/com.github.RedMadRobot/input-mask-android
    api("com.github.RedMadRobot:input-mask-android:6.1.0")

    val koinVersion = "3.2.0"
    // https://github.com/InsertKoinIO/koin
    api("io.insert-koin:koin-android:$koinVersion")
    implementation("io.insert-koin:koin-core:$koinVersion")

    api("io.insert-koin:koin-androidx-workmanager:$koinVersion")

    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    val retrofitVersion = "2.9.0"
    api("com.squareup.retrofit2:retrofit:$retrofitVersion")
    api("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    api("com.squareup.retrofit2:converter-scalars:$retrofitVersion")

    val okHttpVersion = "4.9.3"
    api("com.squareup.okhttp3:okhttp:$okHttpVersion")
    api("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    api("com.squareup.retrofit2:adapter-rxjava2:$retrofitVersion")
    api("com.squareup.retrofit2:converter-scalars:$retrofitVersion")
    api("com.google.code.gson:gson:$retrofitVersion")

    api(Dependencies.timber)

    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")

    // Sentry
    val sentryVersion = "6.14.0"
    implementation("io.sentry:sentry-android:$sentryVersion")
    implementation("io.sentry:sentry-android-fragment:$sentryVersion")
    implementation("io.sentry:sentry-android-core:$sentryVersion")
    implementation("io.sentry:sentry-android-okhttp:$sentryVersion")
    implementation("io.sentry:sentry-android-ndk:$sentryVersion")

}
