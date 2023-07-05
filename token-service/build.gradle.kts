plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
}

android {
    buildFeatures.buildConfig = true
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core"))

    val koin_version = "3.4.1"
    testImplementation("io.insert-koin:koin-test:$koin_version")
    // Koin for JUnit 4
    testImplementation("io.insert-koin:koin-test-junit4:$koin_version")
    // Koin for JUnit 5
    testImplementation("io.insert-koin:koin-test-junit5:$koin_version")

    val coroutines_version = "1.7.1"
    testImplementation("androidx.test:core-ktx:1.5.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.5")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    // https://github.com/mockk/mockk
    testImplementation("io.mockk:mockk:1.12.0")
    // https://mvnrepository.com/artifact/junit/junit
    testImplementation("junit:junit:4.13.2")
    // https://github.com/robolectric/robolectric
    testImplementation("org.robolectric:robolectric:4.7.3")

    implementation("com.lokalise.android:sdk:2.1.1") {
        isTransitive = true
    }
    // Coroutines support
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")

    // Koin Test features
    val koinVersion = "3.2.0"
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit4:$koinVersion")
    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)

}
