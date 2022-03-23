package org.p2p.wallet.infrastructure.navigation

import org.p2p.wallet.utils.emptyString
import timber.log.Timber

class NavigationScreenTracker {

    @Volatile
    private var currentScreen: String = emptyString()

    fun setCurrentScreen(screenName: String) {
        currentScreen = screenName
        Timber.tag("ScreenTracker").d("Current screen is $screenName")
    }

    fun getCurrentScreen(): String = currentScreen
}
