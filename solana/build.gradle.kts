plugins {
    id("org.p2p.wallet.android.library")
}

dependencies {
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }

    implementation(project(":core"))
    implementation("net.i2p.crypto:eddsa:0.3.0")
    implementation("com.google.firebase:firebase-crashlytics-buildtools:2.9.4")


    val coroutinesVersion = "1.6.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
    implementation("com.squareup.retrofit2:converter-moshi:2.7.1")

    api("org.java-websocket:Java-WebSocket:1.5.2")

    // core
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    // https://github.com/mockk/mockk
    testImplementation("io.mockk:mockk:1.12.0")
    // https://mvnrepository.com/artifact/junit/junit
    testImplementation("junit:junit:4.13.2")
    // https://github.com/robolectric/robolectric
    testImplementation("org.robolectric:robolectric:4.7.3")

    val koinVersion = "3.1.5"
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    //https://github.com/mockito/mockito-kotlin
    testImplementation("org.mockito.kotlin:mockito-kotlin:4.0.0")
    testImplementation("org.mockito:mockito-inline:3.8.0")
    testImplementation("org.assertj:assertj-core:3.22.0")
}
