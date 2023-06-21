plugins {
    id("org.p2p.wallet.android.library")
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
    api("io.insert-koin:koin-androidx-workmanager:$koinVersion")

    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("com.squareup.retrofit2:adapter-rxjava2:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
    implementation("com.google.code.gson:gson:2.9.0")

    val scarlet_version = "0.1.12"
    implementation("com.tinder.scarlet:scarlet:$scarlet_version")
    implementation("com.tinder.scarlet:websocket-okhttp:$scarlet_version")
    implementation("com.tinder.scarlet:stream-adapter-rxjava2:$scarlet_version")
    implementation("com.tinder.scarlet:message-adapter-gson:$scarlet_version")
    implementation("com.tinder.scarlet:lifecycle-android:$scarlet_version")
    // https://github.com/JakeWharton/timber
    api(Dependencies.timber)

}
