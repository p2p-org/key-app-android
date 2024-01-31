package org.p2p.wallet.android

import AppVersions
import BuildConfiguration
import Versions
import com.android.build.gradle.internal.api.BaseVariantOutputImpl
import gradle.kotlin.dsl.accessors._4b7ad2363fc1fce7c774e054dc9a9300.java

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.parcelize")
    id("com.google.firebase.appdistribution")
    kotlin("android")
    kotlin("kapt")
}

apply {
    from("${project.rootDir}/.scripts/ktlint.gradle")
    from("${project.rootDir}/.scripts/versioning.gradle")
    from("${project.rootDir}/.scripts/signing.gradle")
    from("${project.rootDir}/.scripts/analytics.gradle")
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = Versions.sdkCompileVersion

    defaultConfig {
        applicationId = "org.p2p.wallet"
        minSdk = Versions.sdkMinVersion
        targetSdk = Versions.sdkTargetVersion
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME

        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        setProperty("archivesBaseName", Versions.generateReleaseAppName())
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = true
            versionNameSuffix = ".${AppVersions.VERSION_BUILD}-debug"
        }

        getByName("feature") {
            applicationIdSuffix = ".feature"
            signingConfig = signingConfigs.getByName("debug")
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = false
            versionNameSuffix = ".${AppVersions.VERSION_BUILD}-feature"
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = BuildConfiguration.getChangelogFilePath(project.rootDir.absolutePath)
                groups = BuildConfiguration.FEATURE_BUILD_TESTERS_GROUP
            }

            matchingFallbacks += listOf("debug")
        }

        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true

            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")

            firebaseAppDistribution {
                releaseNotesFile = BuildConfiguration.getChangelogFilePath(project.rootDir.absolutePath)
                groups = BuildConfiguration.RELEASE_BUILD_TESTERS_GROUP
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    applicationVariants.all {
        outputs
            .map { it as BaseVariantOutputImpl }
            .forEach { output ->
                output.outputFileName = Versions.generateDebugAppName() + ".apk"
            }
    }

    buildFeatures {
        viewBinding = true
    }
}
