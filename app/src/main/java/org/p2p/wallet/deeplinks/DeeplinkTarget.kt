package org.p2p.wallet.deeplinks

import org.p2p.uikit.components.ScreenTab

/**
 * Pre-defined deeplink targets associated with screen names
 */
enum class DeeplinkTarget(val screenName: String, val tab: ScreenTab? = null) {
    HISTORY(screenName = "history", tab = ScreenTab.HISTORY_SCREEN),
    SWAP(screenName = "swap", tab = ScreenTab.SWAP_SCREEN),
    SETTINGS(screenName = "settings", tab = ScreenTab.SETTINGS_SCREEN),
    SEND(screenName = "send"),
    BUY(screenName = "buy"),
    CASH_OUT(screenName = "cashOut"),
    REFERRAL(screenName = "main"),

    /**
     * requires intercom_survey_id @see [org.p2p.wallet.intercom.IntercomDeeplinkManager]
     */
    FEEDBACK(screenName = "intercom", ScreenTab.FEEDBACK_SCREEN),
    ONBOARDING(screenName = "onboarding");

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
