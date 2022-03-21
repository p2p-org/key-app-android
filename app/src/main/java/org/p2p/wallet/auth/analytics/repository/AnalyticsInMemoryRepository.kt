package org.p2p.wallet.auth.analytics.repository

import org.p2p.wallet.utils.emptyString

class AnalyticsInMemoryRepository : AnalyticsLocalRepository {

    private val openedScreenList = mutableListOf<String>()

    override fun onScreenChanged(screenName: String) {
        openedScreenList.add(screenName)
    }

    override fun getCurrentScreenName(): String = try {
        openedScreenList[openedScreenList.lastIndex]
    } catch (e: ArrayIndexOutOfBoundsException) {
        emptyString()
    }

    override fun getPreviousScreenName(): String = try {
        openedScreenList[openedScreenList.lastIndex - 1]
    } catch (e: ArrayIndexOutOfBoundsException) {
        emptyString()
    }
}