import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.testImplementation
import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.testRuntimeOnly
import org.gradle.kotlin.dsl.DependencyHandlerScope
import org.gradle.kotlin.dsl.project

object Dependencies {

    // Kotlin
    const val kotlinStdlib = "org.jetbrains.kotlin:kotlin-stdlib-jdk8:${Versions.kotlinVersion}"
    const val kotlinReflect = "org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlinVersion}"
    const val kotlinGradlePlugin = "org.jetbrains.kotlin:kotlin-gradle-plugin:${Versions.kotlinVersion}"

    private const val androidxCoreKtxVersion = "1.10.1"
    private const val appCompatVersion = "1.6.0"
    private const val materialDesignVersion = "1.8.0"
    private const val recyclerviewVersion = "1.2.1"
    private const val activityKtxVersion = "1.6.0"
    private const val fragmentKtxVersion = "1.4.1"

    private const val coreKtx = "androidx.core:core-ktx:$androidxCoreKtxVersion"
    private const val splash = "androidx.core:core-splashscreen:1.0.1"
    private const val appCompat = "androidx.appcompat:appcompat:$appCompatVersion"
    private const val material = "com.google.android.material:material:$materialDesignVersion"
    private const val recyclerView = "androidx.recyclerview:recyclerview:$recyclerviewVersion"
    private const val activityKtx = "androidx.activity:activity-ktx:$activityKtxVersion"
    private const val fragmentKtx = "androidx.fragment:fragment-ktx:$fragmentKtxVersion"
    private const val biometricKtx = "androidx.biometric:biometric:1.1.0"
    private const val browser = "androidx.browser:browser:1.5.0"
    const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
    const val flexbox = "com.google.android.flexbox:flexbox:3.0.0"

    //https://facebook.github.io/shimmer-android/
    const val shimmer = "com.facebook.shimmer:shimmer:0.5.0"

    // https://mvnrepository.com/artifact/org.bitcoinj/bitcoinj-core
    const val bitcoinj = "org.bitcoinj:bitcoinj-core:0.15.10"

    // coroutines
    private const val coroutinesVersion = "1.6.2"
    private const val coroutinesCore = "org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion"
    private const val coroutinesAndroid = "org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion"

    // gson
    private const val gsonVersion = "2.9.0"
    const val gson = "com.google.code.gson:gson:${gsonVersion}"

    // retrofit
    private const val retrofitVersion = "2.9.0"
    private const val retrofit = "com.squareup.retrofit2:retrofit:$retrofitVersion"
    private const val retrofitGson = "com.squareup.retrofit2:converter-gson:$retrofitVersion"
    private const val retrofitScalars = "com.squareup.retrofit2:converter-scalars:$retrofitVersion"
    const val retrofitMoshi = "com.squareup.retrofit2:converter-moshi:$retrofitVersion"

    private const val okHttpVersion = "4.9.3"
    private const val okHttp = "com.squareup.okhttp3:okhttp:$okHttpVersion"
    private const val okHttpLoggingInterceptor = "com.squareup.okhttp3:logging-interceptor:$okHttpVersion"

    // WS
    const val javaWs = "org.java-websocket:Java-WebSocket:1.5.2"

    // Tinder Scarlet
    private const val scarletVersion = "0.1.12"
    private const val tinderScarlet = "com.tinder.scarlet:scarlet:$scarletVersion"
    private const val tinderScarletWs = "com.tinder.scarlet:websocket-okhttp:$scarletVersion"
    private const val tinderScarletRxJava2 = "com.tinder.scarlet:stream-adapter-rxjava2:$scarletVersion"
    private const val tinderScarletGson = "com.tinder.scarlet:message-adapter-gson:$scarletVersion"
    private const val tinderScarletLifecycle = "com.tinder.scarlet:lifecycle-android:$scarletVersion"

    // Room
    private const val roomVersion = "2.4.3"
    const val roomRuntime = "androidx.room:room-runtime:$roomVersion"
    const val roomKtx = "androidx.room:room-ktx:$roomVersion"
    const val roomCompiler = "androidx.room:room-compiler:$roomVersion"

    // Koin https://github.com/InsertKoinIO/koin
    private const val koinVersion = "3.2.0"
    private const val koinAndroid = "io.insert-koin:koin-android:$koinVersion"
    private const val koinWorkmanager = "io.insert-koin:koin-androidx-workmanager:$koinVersion"

    // firebase
    const val firebaseBom = "com.google.firebase:firebase-bom:32.1.1"
    const val firebaseCrashlytics = "com.google.firebase:firebase-crashlytics"
    private const val firebaseAnalytics = "com.google.firebase:firebase-analytics-ktx"
    private const val firebaseMessaging = "com.google.firebase:firebase-messaging-ktx"
    private const val firebaseConfig = "com.google.firebase:firebase-config"
    const val firebaseCrashlyticsBuildTools = "com.google.firebase:firebase-crashlytics-buildtools:2.9.6"

    // Sentry
    private const val sentryVersion = "6.14.0"
    private const val sentry = "io.sentry:sentry-android:$sentryVersion"
    private const val sentryFragment = "io.sentry:sentry-android-fragment:$sentryVersion"
    private const val sentryCore = "io.sentry:sentry-android-core:$sentryVersion"
    private const val sentryOkhttp = "io.sentry:sentry-android-okhttp:$sentryVersion"
    private const val sentryNdk = "io.sentry:sentry-android-ndk:$sentryVersion"

    // google play
    private const val googlePlayBase = "com.google.android.gms:play-services-base:18.2.0"
    private const val googlePlayAuth = "com.google.android.gms:play-services-auth:20.6.0"

    // glide
    private const val glideVersion = "4.15.1"
    const val glide = "com.github.bumptech.glide:glide:$glideVersion"
    const val glideCompiler = "com.github.bumptech.glide:compiler:$glideVersion"
    const val caverockSvg = "com.caverock:androidsvg-aar:1.4"

    // adapter delegates
    private const val adapterDelegatesVersion = "4.3.2"
    private const val adapterDelegates = "com.hannesdorfmann:adapterdelegates4-kotlin-dsl:$adapterDelegatesVersion"
    private const val adapterDelegatesBinding =
        "com.hannesdorfmann:adapterdelegates4-kotlin-dsl-viewbinding:$adapterDelegatesVersion"

    // AppsFlyer
    private const val appsFlyerSdk = "com.appsflyer:af-android-sdk:6.9.0"
    private const val androidInstallReferrer = "com.android.installreferrer:installreferrer:2.2"
    private const val androidAdsIndetifier = "androidx.ads:ads-identifier:1.0.0-alpha05"

    // Striga
    const val strigaSdk = "com.sumsub.sns:idensic-mobile-sdk:1.25.0"

    // Crypto
    const val cryptoEdds = "net.i2p.crypto:eddsa:0.3.0"

    // Utils
    const val lokalise = "com.lokalise.android:sdk:2.1.1"
    const val libphonenumber = "io.michaelrocks:libphonenumber-android:8.12.52"
    const val workRuntimeKtx = "androidx.work:work-runtime-ktx:2.7.1"
    const val intercom = "io.intercom.android:intercom-sdk:14.0.0"
    const val amplitude = "com.amplitude:android-sdk:2.35.3"
    const val lottie = "com.airbnb.android:lottie:4.0.0"
    const val dotsIndicator = "com.tbuonomo:dotsindicator:4.3"
    const val tickerView = "com.robinhood.ticker:ticker:2.0.4"

    // https://mvnrepository.com/artifact/com.github.RedMadRobot/input-mask-android
    const val inputmask = "com.github.RedMadRobot:input-mask-android:6.1.0"

    // https://github.com/dm77/barcodescanner
    const val barcodeScanner = "me.dm7.barcodescanner:zxing:1.9.8"

    // https://github.com/JakeWharton/ThreeTenABP
    const val threetenabp = "com.jakewharton.threetenabp:threetenabp:1.3.0"
    const val debugDrawer = "io.palaima.debugdrawer:debugdrawer-timber:0.8.0"

    // timber
    private const val timberVersion = "5.0.1"
    const val timber = "com.jakewharton.timber:timber:$timberVersion"

    // Unit Testing
    // core
    private const val coreTestKtx = "androidx.test:core-ktx:1.5.0"
    private const val junitKtx = "androidx.test.ext:junit-ktx:1.1.5"
    private const val coreTesting = "androidx.arch.core:core-testing:2.2.0"

    // https://github.com/mockk/mockk
    private const val mockk = "io.mockk:mockk:1.12.0"

    // https://mvnrepository.com/artifact/junit/junit
    const val junit = "junit:junit:4.13.2"

    const val assertj = "org.assertj:assertj-core:3.22.0"

    // https://github.com/robolectric/robolectric
    private const val roboletric = "org.robolectric:robolectric:4.7.3"

    // Coroutines support
    private const val coroutinesTest = "org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutinesVersion"

    // Koin Test features
    private const val koinTest = "io.insert-koin:koin-test:$koinVersion"
    private const val koinTestJunit = "io.insert-koin:koin-test-junit4:$koinVersion"

    private const val assertKTest = "com.willowtreeapps.assertk:assertk-jvm:0.25"
    private const val slf4jTest = "org.slf4j:slf4j-nop:1.7.30"
    private const val jupiterJunit = "org.junit.jupiter:junit-jupiter-api:5.8.2"

    // runtime only
    const val junitPlatform = "org.junit.platform:junit-platform-launcher:1.8.2"
    private const val junitJupiterVersion = "5.8.2"
    private const val junitEngine = "org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion"
    private const val junitVintageEngine = "org.junit.vintage:junit-vintage-engine:$junitJupiterVersion"

    // timezone for unit testing
    private const val threetenTest = "org.threeten:threetenbp:1.6.8"

    // XmlPullParser goes with android.jar and doesn't work in unit tests
    private const val xmlPull = "xmlpull:xmlpull:1.1.3.4a@jar"

    // KXmlParser needs for XmlPullParserFactory
    private const val kxml = "net.sf.kxml:kxml2:2.3.0"

    val baseAndroidLibraries = listOf(
        coreKtx,
        splash,
        appCompat,
        material,
        recyclerView,
        activityKtx,
        fragmentKtx,
        biometricKtx,
        browser
    )

    val coroutineLibraries = listOf(
        coroutinesCore,
        coroutinesAndroid,
    )

    val retrofitLibraries = listOf(
        retrofit,
        retrofitGson,
        retrofitScalars,
        okHttp,
        okHttpLoggingInterceptor,
        gson,
    )

    val tinderScarletLibraries = listOf(
        tinderScarlet,
        tinderScarletWs,
        tinderScarletRxJava2,
        tinderScarletGson,
        tinderScarletLifecycle
    )

    val koinLibraries = listOf(
        koinAndroid,
        koinWorkmanager
    )

    val firebaseLibraries = listOf(
        firebaseCrashlytics,
        firebaseAnalytics,
        firebaseMessaging,
        firebaseConfig
    )

    val sentryLibraries = listOf(
        sentry,
        sentryFragment,
        sentryCore,
        sentryOkhttp,
        sentryNdk
    )

    val googlePlayLibraries = listOf(
        googlePlayBase,
        googlePlayAuth,
    )

    val adapterDelegatesLibraries = listOf(
        adapterDelegates,
        adapterDelegatesBinding,
    )

    val appsFlyerLibraries = listOf(
        appsFlyerSdk,
        androidInstallReferrer,
        androidAdsIndetifier
    )

    val coreTestingLibraries = listOf(
        coreTestKtx,
        junitKtx,
        coreTesting,
        mockk,
        junit,
        roboletric,
        coroutinesTest
    )

    val koinTestingLibraries = listOf(
        koinTest,
        koinTestJunit
    )

    val junitRuntimeOnlyLibraries = listOf(
        junitEngine,
        junitVintageEngine
    )

    val otherTestingLibraries = listOf(
        assertKTest,
        slf4jTest,
        jupiterJunit,
        threetenTest,
        xmlPull,
        kxml
    )

    fun testUtilsModule(scope: DependencyHandlerScope) {
        with(scope) {
            testImplementation(project(":test-utils"))

            // Core testing
            coreTestingLibraries.forEach { testImplementation(it) }
            // Koin testing
            koinTestingLibraries.forEach { testImplementation(it) }
            // Other testing tools
            otherTestingLibraries.forEach { testImplementation(it) }

            // Runtime only testing tools
            testRuntimeOnly(junitPlatform) {
                because(
                    "This lib comes shipped with the IDE and it possible that newer versions of JUnit 5 maybe " +
                        "be incompatible with the version of junit-platform-launcher shipped with the IDE."
                )
            }
            junitRuntimeOnlyLibraries.forEach { testRuntimeOnly(it) }
        }
    }
}
