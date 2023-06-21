plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
}

android {
    buildFeatures.buildConfig = true
}
dependencies {

    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))
    implementation("com.google.firebase:firebase-crashlytics")

    // Sentry
    val sentryVersion = "6.14.0"
    implementation("io.sentry:sentry-android:$sentryVersion")
    implementation("io.sentry:sentry-android-fragment:$sentryVersion")
    implementation("io.sentry:sentry-android-core:$sentryVersion")
    implementation("io.sentry:sentry-android-okhttp:$sentryVersion")
    implementation("io.sentry:sentry-android-ndk:$sentryVersion")

    implementation(Dependencies.timber)

}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
repositories {
    mavenCentral()
}
