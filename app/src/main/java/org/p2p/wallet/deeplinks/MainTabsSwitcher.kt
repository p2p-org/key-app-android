package org.p2p.wallet.deeplinks

import org.p2p.uikit.components.ScreenTab

interface MainTabsSwitcher {
    fun navigate(clickedTab: ScreenTab)
}
