package org.p2p.core.analytics.repository

class AnalyticsInMemoryRepository : AnalyticsLocalRepository {

    private val openedScreenList = mutableListOf<String>()

    override fun changeCurrentScreen(newScreenName: String) {
        // to remove duplicates when onResume is called
        if (newScreenName != getCurrentScreenName()) {
            openedScreenList.add(newScreenName)
        }
    }

    override fun getCurrentScreenName(): String =
        openedScreenList.lastOrNull().orEmpty()

    override fun getPreviousScreenName(): String =
        openedScreenList.getOrNull(openedScreenList.lastIndex - 1).orEmpty()
}
