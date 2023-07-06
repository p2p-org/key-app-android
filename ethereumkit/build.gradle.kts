plugins {
    id("org.p2p.wallet.android.library")
}

android {
    namespace = "org.p2p.ethereumkit"
}

dependencies {
    implementation(project(":core"))
    implementation("org.bouncycastle:bcpkix-jdk15on:1.68")

    implementation("com.github.horizontalsystems:hd-wallet-kit-android:f46885a")

    implementation("io.reactivex.rxjava2:rxjava:2.2.19")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

//Eip712
    implementation("org.web3j:crypto:4.9.6")

// Test helpers
    testImplementation("junit:junit:4.13.2")

// Spek

// Android Instrumentation Test
}
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}
repositories {
    mavenCentral()
}
