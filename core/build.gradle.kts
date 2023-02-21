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
}
