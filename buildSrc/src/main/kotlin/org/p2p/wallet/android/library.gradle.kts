package org.p2p.wallet.android

import Dependencies
import Versions
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.plugin.parcelize")
    kotlin("android")
}

android {
    compileSdk = Versions.sdkCompileVersion

    defaultConfig {
        minSdk = Versions.sdkMinVersion
        targetSdk = Versions.sdkTargetVersion

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        getByName("debug") {
        }

        create("feature") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }

        getByName("release") {
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
        animationsDisabled = true
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.all {
            it.useJUnitPlatform()
            it.maxParallelForks = Runtime.getRuntime().availableProcessors().div(2)
            it.maxHeapSize = "512m"
            it.testLogging {
                events = setOf(TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            }
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = false
    }

    sourceSets {
        named("main") {
            java.srcDirs("src/main/kotlin")
            res.srcDirs("src/main/res")
        }

        named("debug") {
            java.srcDirs("src/debug/kotlin")
            res.srcDirs("src/debug/res")
        }

        named("feature") {
            java.srcDirs("src/feature/kotlin")
            res.srcDirs("src/feature/res")
        }

        named("release") {
            java.srcDirs("src/release/kotlin")
            res.srcDirs("src/release/res")
        }

        named("androidTest") {
            assets.srcDirs(files("$projectDir/schemas".toString()))
        }
    }
}

dependencies {
    implementation(Dependencies.kotlinStdlib)
    implementation(Dependencies.kotlinReflect)
}
