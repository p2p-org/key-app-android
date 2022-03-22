package org.p2p.wallet.infrastructure.navigation

import timber.log.Timber

class NavigationScreenTracker {

    @Volatile
    private var currentScreen: String = ""

    fun setCurrentScreen(screenName: String) {
        currentScreen = screenName
        Timber.tag("ScreenTracker").d("Current screen is $screenName")
    }

    fun getCurrentScreen(): String = currentScreen
}
