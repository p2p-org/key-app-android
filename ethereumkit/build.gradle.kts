plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
}

android {
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf(
                    "room.schemaLocation" to "$projectDir/schemas",
                    "room.incremental" to "true"
                )
            }
        }
    }
}

dependencies {
    implementation(project(":core"))
    implementation("org.bouncycastle:bcpkix-jdk15on:1.68")

    implementation("com.github.horizontalsystems:hd-wallet-kit-android:f46885a")

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

// Room
    implementation("androidx.room:room-runtime:2.4.3")
    implementation("androidx.room:room-rxjava2:2.4.3")
    kapt("androidx.room:room-compiler:2.4.3")

//Eip712
    implementation("org.web3j:crypto:4.9.6")

// Test helpers
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:3.3.3")
    testImplementation("com.nhaarman:mockito-kotlin-kt1.1:1.6.0")
    testImplementation("org.powermock:powermock-api-mockito2:2.0.7")
    testImplementation("org.powermock:powermock-module-junit4:2.0.7")

// Spek
    testImplementation("org.spekframework.spek2:spek-dsl-jvm:2.0.9")
    testRuntimeOnly("org.spekframework.spek2:spek-runner-junit5:2.0.9")
    testRuntimeOnly("org.jetbrains.kotlin:kotlin-reflect:1.7.20")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.7.20")

// Android Instrumentation Test
    implementation("androidx.annotation:annotation:1.4.0")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("com.linkedin.dexmaker:dexmaker-mockito-inline:2.19.1")
    androidTestImplementation("com.nhaarman:mockito-kotlin-kt1.1:1.6.0")

    testImplementation( "io.insert-koin:koin-test:3.1.5")
    // Needed JUnit version
    testImplementation( "io.insert-koin:koin-test-junit4:3.1.5")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.6.4")

}
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
repositories {
    mavenCentral()
}
