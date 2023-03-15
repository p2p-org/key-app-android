import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("org.p2p.wallet.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("io.sentry.android.gradle") version "3.2.0"
}

android {
    applicationVariants.all {
        outputs
            .map { it as BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = Versions.CURRENT_APP_NAME + "-${buildType.name}.apk"
            }
    }

    buildFeatures { viewBinding = true }

    sentry {
        autoInstallation { enabled.set(false) }
    }

    bundle {
        language { enableSplit = false }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.all {
            it.maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
            it.maxHeapSize = "512m"
            it.testLogging {
                events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            }
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if(requested.group == "org.bouncycastle") {
                useTarget("org.bouncycastle:bcprov-jdk15to18:1.68")
            }
        }
    }
}

dependencies {
    implementation(project(":solana"))
    implementation(project(":ui-kit"))
    implementation(project(":core"))
    implementation(project(":ethereumkit"))

    implementation(files("libs/borshj-0.0.0.jar"))

    Dependencies.baseAndroidLibraries.forEach { implementation(it) }
    Dependencies.adapterDelegatesLibraries.forEach { implementation(it) }

    implementation("androidx.biometric:biometric:1.1.0")
    implementation("androidx.browser:browser:1.4.0")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}")

    val coroutinesVersion = "1.6.2"
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")

    val work_version = "2.7.1"
    implementation("androidx.work:work-runtime-ktx:$work_version")

    // https://github.com/JakeWharton/timber
    implementation("com.jakewharton.timber:timber:5.0.1")
    // https://github.com/dm77/barcodescanner
    implementation("me.dm7.barcodescanner:zxing:1.9.8")
    // https://github.com/JakeWharton/ThreeTenABP
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.0")

    val retrofitVersion = "2.9.0"
    implementation("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation ("com.squareup.retrofit2:converter-scalars:$retrofitVersion")


    val okHttpVersion = "4.9.3"
    implementation("com.squareup.okhttp3:okhttp:$okHttpVersion")
    implementation("com.squareup.okhttp3:logging-interceptor:$okHttpVersion")

    // Import the BoM for the Firebase platform
    implementation(platform("com.google.firebase:firebase-bom:31.1.1"))

    // Declare the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-crashlytics")
    implementation("com.google.firebase:firebase-analytics-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-config")

    implementation("io.intercom.android:intercom-sdk:14.0.0")

    implementation("com.amplitude:android-sdk:2.35.3")

    implementation("com.google.android.gms:play-services-base:18.1.0")

    // Debug drawer
    val debugDrawerVersion = "0.8.0"
    implementation("io.palaima.debugdrawer:debugdrawer-timber:$debugDrawerVersion")

    // Glide
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("com.caverock:androidsvg-aar:1.4")

    // Room
    val roomVersion = "2.4.3"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Lottie
    val lottieVersion = "4.0.0"
    implementation("com.airbnb.android:lottie:$lottieVersion")

    // Sentry
    val sentryVersion = "6.14.0"
    implementation("io.sentry:sentry-android:$sentryVersion")
    implementation("io.sentry:sentry-android-fragment:$sentryVersion")
    implementation("io.sentry:sentry-android-core:$sentryVersion")
    implementation("io.sentry:sentry-android-okhttp:$sentryVersion")
    implementation("io.sentry:sentry-android-ndk:$sentryVersion")

    // dots indicator
    implementation("com.tbuonomo:dotsindicator:4.3")

    // Google Play Auth
    implementation("com.google.android.gms:play-services-auth:20.3.0")

    // AppsFlyer
    implementation("com.appsflyer:af-android-sdk:6.9.0")
    implementation("androidx.ads:ads-identifier:1.0.0-alpha04")
    implementation("com.android.installreferrer:installreferrer:2.2")

    // TickerView
    implementation("com.robinhood.ticker:ticker:2.0.4")
    implementation("androidx.core:core-splashscreen:1.0.0")

    implementation("io.michaelrocks:libphonenumber-android:8.12.52")

    // core
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    // https://github.com/mockk/mockk
    testImplementation("io.mockk:mockk:1.12.0")
    // https://mvnrepository.com/artifact/junit/junit
    testImplementation("junit:junit:4.13.2")
    // https://github.com/robolectric/robolectric
    testImplementation("org.robolectric:robolectric:4.5.1")

    implementation("com.lokalise.android:sdk:2.1.1") {
        isTransitive = true
    }
    // Coroutines support
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion")

    // Koin Test features
    val koinVersion = "3.2.0"
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit4:$koinVersion")

    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.slf4j:slf4j-nop:1.7.30")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.assertj:assertj-core:3.22.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.8.2") {
        because("This lib comes shipped with the IDE and it possible that newer versions of JUnit 5 maybe be incompatible with the version of junit-platform-launcher shipped with the IDE.")
    }
    val junitJupiterVersion = "5.8.2"
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.vintage:junit-vintage-engine:$junitJupiterVersion")
}
