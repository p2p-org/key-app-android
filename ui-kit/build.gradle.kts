plugins {
    id("org.p2p.wallet.android.library")
}

dependencies {
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }
    Dependencies.adapterDelegatesLibraries.forEach { implementation(it) }

    implementation(project(":core"))
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")

    api("com.google.android.flexbox:flexbox:3.0.0")

    //https://facebook.github.io/shimmer-android/
    api("com.facebook.shimmer:shimmer:0.5.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.assertj:assertj-core:3.22.0")
}
