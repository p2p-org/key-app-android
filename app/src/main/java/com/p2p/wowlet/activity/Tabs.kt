package com.p2p.wowlet.activity

import com.p2p.wowlet.R

enum class Tabs(val tabId: Int) {
    DASHBOARD(R.id.bottom_navigation_dashboard),
    INVESTMENTS(R.id.bottom_navigation_investments),
    SEARCH(R.id.bottom_navigation_search),
    PROFILE(R.id.bottom_navigation_profile);

    companion object {
        fun fromTabId(tabId: Int): Tabs {
            return values()
                .firstOrNull { it.tabId == tabId }
                ?: throw IllegalArgumentException("Unknown tabId: $tabId")
        }
    }
}