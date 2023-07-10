import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.p2p.wallet.android.application")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("io.sentry.android.gradle") version "3.2.0"
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
    id("kotlin-kapt")
}

koverReport {
    androidReports("debug") {
        filters {
            excludes {
                annotatedBy(
                    "*NoCoverage",
                    "*Generated*",
                    "androidx.room.Database",
                )
                packages(
                    "com.bumptech.glide*",
                )
                classes(
                    "*Module",
                    "*Module\$*",
                    "*Fragment",
                    "*Fragment\$*",
                    "*Activity",
                    "*Activity\$*",
                    "*Service",
                    "*Service\$*",
                    "*BottomSheet",
                    "*BottomSheet\$*",
                    "*View",
                    "*View\$*",
                    "*Interceptor",
                    "*Interceptor\$*",
                    "*.databinding.*",
                    "*.BuildConfig",

                    // excludes debug classes
                    "*.DebugUtil"
                )
            }
        }
    }
}

android {
    applicationVariants.all {
        outputs
            .map { it as BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = Versions.generateDebugAppName() + ".apk"
            }
    }

    buildFeatures { viewBinding = true }

    sentry {
        autoInstallation { enabled.set(false) }
        ignoredBuildTypes.addAll("debug", "feature")
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
            if (requested.group == "org.bouncycastle") {
                useTarget("org.bouncycastle:bcprov-jdk15to18:1.68")
            }
        }
    }

    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions {
            jvmTarget = "11"
        }
    }
}

dependencies {
    implementation(project(":solana"))
    implementation(project(":ui-kit"))
    implementation(project(":core"))
    implementation(project(":ethereumkit"))
    implementation(project(":token-service"))

    implementation(files("libs/borshj-0.0.0.jar"))

    implementation(Dependencies.kotlinReflect)

    // Android common
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }

    // Adapter delegates
    Dependencies.adapterDelegatesLibraries.forEach { implementation(it) }

    // Coroutines
    Dependencies.coroutineLibraries.forEach { implementation(it) }

    // Room

    // Firebase
    implementation(platform(Dependencies.firebaseBom))
    Dependencies.firebaseLibraries.forEach { implementation(it) }

    // Google Play
    Dependencies.googlePlayLibraries.forEach { implementation(it) }

    // Glide
    implementation(Dependencies.glide)
    implementation(Dependencies.caverockSvg)
    kapt(Dependencies.glideCompiler)

    // Lottie
    implementation(Dependencies.lottie)

    // dots indicator
    implementation(Dependencies.dotsIndicator)

    // AppsFlyer
    Dependencies.appsFlyerLibraries.forEach { implementation(it) }

    // Striga SDK https://developers.sumsub.com/msdk/android/changelog.html
    implementation(Dependencies.strigaSdk)

    // Utils
    implementation(Dependencies.libphonenumber)
    implementation(Dependencies.lokalise) { isTransitive = true }
    implementation(Dependencies.workRuntimeKtx)
    implementation(Dependencies.barcodeScanner)
    implementation(Dependencies.threetenabp)
    implementation(Dependencies.intercom)
    implementation(Dependencies.amplitude)
    implementation(Dependencies.debugDrawer)
    implementation(Dependencies.tickerView)
    implementation(Dependencies.bitcoinj)


    // Core testing
    Dependencies.coreTestingLibraries.forEach { testImplementation(it) }

    // Koin testing
    Dependencies.koinTestingLibraries.forEach { testImplementation(it) }

    // Other testing tools
    Dependencies.otherTestingLibraries.forEach { testImplementation(it) }

    // Runtime only testing tools
    testRuntimeOnly(Dependencies.junitPlatform) {
        because("This lib comes shipped with the IDE and it possible that newer versions of JUnit 5 maybe be incompatible with the version of junit-platform-launcher shipped with the IDE.")
    }
    Dependencies.junitRuntimeOnlyLibraries.forEach { testRuntimeOnly(it) }

    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)
}
