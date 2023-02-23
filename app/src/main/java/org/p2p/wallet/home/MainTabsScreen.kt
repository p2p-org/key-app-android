package org.p2p.wallet.home

import androidx.fragment.app.Fragment
import org.p2p.wallet.deeplinks.MainTabsSwitcher

interface MainTabsScreen : MainTabsSwitcher

fun Fragment.isInMainTabsScreen(): Boolean {
    var parentFragment: Fragment? = parentFragment
    while (parentFragment != null) {
        val isMainTabsScreen = parentFragment is MainTabsScreen
        if (isMainTabsScreen) return true else parentFragment = parentFragment.parentFragment
    }
    return false
}
