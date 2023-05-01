object Dependencies {

    private const val androidxCoreKtxVersion = "1.9.0"
    private const val appCompatVersion = "1.6.0"
    private const val materialDesignVersion = "1.8.0"
    private const val recyclerviewVersion = "1.2.1"
    private const val activityKtxVersion = "1.6.0"
    private const val fragmentKtxVersion = "1.4.1"
    private const val adapterDelegatesVersion = "4.3.2"

    private const val androidBuildToolsVersion = "7.4.1"

    // Kotlin
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"

    const val coreKtx = "androidx.core:core-ktx:$androidxCoreKtxVersion"
    const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
    const val material = "com.google.android.material:material:$materialDesignVersion"
    const val recyclerView = "androidx.recyclerview:recyclerview:$recyclerviewVersion"
    const val activityKtx = "androidx.activity:activity-ktx:$activityKtxVersion"
    const val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentKtxVersion"
    const val adapterDelegates = "com.hannesdorfmann:adapterdelegates4-kotlin-dsl:$adapterDelegatesVersion"
    const val adapterDelegatesBinding = "com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:$adapterDelegatesVersion"

    const val androidBuildTools = "com.android.tools.build.gradle:$androidBuildToolsVersion"

    val baseAndroidLibraries = listOf(
        coreKtx,
        appCompat,
        material,
        recyclerView,
        activityKtx,
        fragmentKtx
    )

    val adapterDelegatesLibraries = listOf(
        adapterDelegates,
        adapterDelegatesBinding,
    )
}
