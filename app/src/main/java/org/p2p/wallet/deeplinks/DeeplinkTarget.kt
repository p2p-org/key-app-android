package org.p2p.wallet.deeplinks

import org.p2p.uikit.components.ScreenTab

/**
 * Pre-defined deeplink targets associated with screen names
 */
enum class DeeplinkTarget(val screenName: String, val tab: ScreenTab? = null) {
    HISTORY("history", ScreenTab.HISTORY_SCREEN),
    SWAP("swap", ScreenTab.SWAP_SCREEN),
    SETTINGS("settings", ScreenTab.SETTINGS_SCREEN),
    SEND("send"),
    BUY("buy"),
    CASH_OUT("cashOut"),

    /**
     * requires intercom_survey_id @see [org.p2p.wallet.intercom.IntercomDeeplinkManager]
     */
    FEEDBACK("intercom", ScreenTab.FEEDBACK_SCREEN),
    ONBOARDING("onboarding");

    companion object {
        /**
         * Returns deeplink target by screen name
         */
        fun fromScreenName(screenName: String?): DeeplinkTarget? {
            return values().find { it.screenName == screenName }
        }

        fun fromScreenTabId(tabId: Int): DeeplinkTarget? {
            return values().find { it.tab?.itemId == tabId }
        }

        fun fromScreenTab(tab: ScreenTab): DeeplinkTarget? {
            return values().find { it.tab == tab }
        }
    }
}
