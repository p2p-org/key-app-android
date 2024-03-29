plugins {
    id("org.p2p.wallet.android.library")
}

dependencies {
    Dependencies.baseAndroidLibraries.forEach { implementation(it) }
    Dependencies.adapterDelegatesLibraries.forEach { implementation(it) }

    implementation(project(":core"))
    implementation(Dependencies.swiperefreshlayout)
    api(Dependencies.flexbox)
    api(Dependencies.shimmer)

    testImplementation(Dependencies.junit)
    testImplementation(Dependencies.assertj)
}
