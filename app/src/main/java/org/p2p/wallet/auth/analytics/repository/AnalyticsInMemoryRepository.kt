package org.p2p.wallet.auth.analytics.repository

class AnalyticsInMemoryRepository : AnalyticsLocalRepository {

    private val openedScreenList = mutableListOf<String>()

    override fun onScreenChanged(screenName: String) {
        openedScreenList.add(screenName)
    }

    override fun getCurrentScreenName(): String = try {
        openedScreenList[openedScreenList.lastIndex]
    } catch (e: ArrayIndexOutOfBoundsException) {
        ""
    }

    override fun getPreviousScreenName(): String = try {
        openedScreenList[openedScreenList.lastIndex - 1]
    } catch (e: ArrayIndexOutOfBoundsException) {
        ""
    }
}
