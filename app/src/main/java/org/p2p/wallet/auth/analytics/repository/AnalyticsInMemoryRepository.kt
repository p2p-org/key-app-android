package org.p2p.wallet.auth.analytics.repository

class AnalyticsInMemoryRepository : AnalyticsLocalRepository {

    private val openedScreenList = mutableListOf<String>()

    override fun onScreenChanged(screenName: String) {
        openedScreenList.add(screenName)
    }

    override fun getCurrentScreenName(): String = openedScreenList[openedScreenList.lastIndex]

    override fun getPreviousScreenName(): String = try {
        openedScreenList[openedScreenList.lastIndex - 1]
    } catch (e: IndexOutOfBoundsException) {
        ""
    }
}