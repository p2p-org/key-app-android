package org.p2p.wallet.root

import androidx.core.splashscreen.SplashScreenViewProvider

data class SplashScreenBox(
    val splashScreen: SplashScreenViewProvider,
    val pendingStatusBarStyle: SystemIconsStyle? = null,
    val pendingNavigationBarStyle: SystemIconsStyle? = null,
)
