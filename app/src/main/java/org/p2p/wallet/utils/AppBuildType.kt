package org.p2p.wallet.utils

import org.p2p.wallet.BuildConfig

enum class AppBuildType {
    DEBUG, FEATURE, RELEASE;

    fun isFeatureBuild() = this == FEATURE
    fun isReleaseBuild() = this == RELEASE

    companion object {
        fun getCurrent(): AppBuildType = when (BuildConfig.BUILD_TYPE.lowercase()) {
            "debug" -> DEBUG
            "feature" -> FEATURE
            "release" -> RELEASE
            else -> error("Failed to resolve new build type ${BuildConfig.BUILD_TYPE}")
        }
    }
}
