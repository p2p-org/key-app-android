plugins {
    id("org.p2p.wallet.android.library")
}

dependencies {
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }

    implementation(project(":core"))
    implementation(Dependencies.cryptoEdds)
    implementation(Dependencies.firebaseCrashlyticsBuildTools)


    implementation(Dependencies.retrofitMoshi)

    api(Dependencies.javaWs)

    // Core testing
    Dependencies.coreTestingLibraries.forEach { testImplementation(it) }

    // Koin testing
    Dependencies.koinTestingLibraries.forEach { testImplementation(it) }

    // Other testing tools
    Dependencies.otherTestingLibraries.forEach { testImplementation(it) }
}
