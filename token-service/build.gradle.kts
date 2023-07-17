plugins {
    id("org.p2p.wallet.android.library")
    id("kotlin-kapt")
    id("org.jetbrains.kotlinx.kover") version "0.7.2"
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
                    "*Interceptor",
                    "*Interceptor\$*",
                    "*.BuildConfig",
                    // excludes debug classes
                    "*.DebugUtil"
                )
            }
        }
    }
}

android {
    buildFeatures.buildConfig = true
    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core"))
    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)

    implementation("com.lokalise.android:sdk:2.1.1") {
        isTransitive = true
    }

    Dependencies.testUtilsModule(this)
}
