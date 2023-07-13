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
    implementation(Dependencies.roomRuntime)
    implementation(Dependencies.roomKtx)
    kapt(Dependencies.roomCompiler)

    implementation("com.lokalise.android:sdk:2.1.1") {
        isTransitive = true
    }

    Dependencies.testUtilsModule(this)
}
