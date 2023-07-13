plugins {
    id("org.p2p.wallet.android.library")
}

android {
    buildFeatures.buildConfig = false
    namespace = "org.p2p.wallet.testutils"
}

dependencies {
    implementation(project(":core"))

    // Core testing
    Dependencies.coreTestingLibraries.forEach { implementation(it) }
    // Koin testing
    Dependencies.koinTestingLibraries.forEach { implementation(it) }
    // Other testing tools
    Dependencies.otherTestingLibraries.forEach { implementation(it) }

    // Runtime only testing tools
    testRuntimeOnly(Dependencies.junitPlatform) {
        because(
            "This lib comes shipped with the IDE and it possible that newer versions of JUnit 5 maybe " +
                "be incompatible with the version of junit-platform-launcher shipped with the IDE."
        )
    }
    Dependencies.junitRuntimeOnlyLibraries.forEach { testRuntimeOnly(it) }
}
